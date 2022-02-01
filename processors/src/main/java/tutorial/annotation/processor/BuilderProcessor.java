package tutorial.annotation.processor;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import tutorial.annotation.MakeBuilder;

/**
 * Date: 01/02/22
 * Time: 6:54 pm
 * This file is project specific to annotation-processor-tutorial
 * Author: Pramod Khalkar
 */
public class BuilderProcessor extends AbstractProcessor {

    private static final String BUILDER_PREFIX = "Builder";
    private Elements allElements;
    private Filer filer;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> allSupportedAnnotationTypes = new LinkedHashSet<>();
        allSupportedAnnotationTypes.add(MakeBuilder.class.getCanonicalName());
        return allSupportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.allElements = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> allSupportedElements = roundEnv.getElementsAnnotatedWith(MakeBuilder.class);
        try {
            for (Element element : allSupportedElements) {
                processEachElement(element);
            }
        } catch (AnnotationProcessException ex) {
            messager.printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), ex.getElement());
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    private void processEachElement(Element element) throws AnnotationProcessException, IOException {
        if (element.getKind() != ElementKind.CLASS) {
            throw new AnnotationProcessException(element, "@%s only supported for class", MakeBuilder.class.getSimpleName());
        } else {
            TypeElement annotatedClass = (TypeElement) element;
            if (!annotatedClass.getModifiers().contains(PUBLIC) && annotatedClass.getModifiers().contains(Modifier.FINAL)) {
                throw new AnnotationProcessException(annotatedClass, "%s should be public and non-final", annotatedClass.getSimpleName());
            } else {
                for (Element enclosedElement : annotatedClass.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.FIELD) {
                        if (!enclosedElement.getModifiers().contains(PUBLIC)) {
                            throw new AnnotationProcessException(enclosedElement, "%s should be public", enclosedElement.getSimpleName());
                        }
                    }
                }
                generateCode(annotatedClass);
            }
        }
    }

    private void generateCode(TypeElement validAnnotedClass) throws IOException {
        String pkg = allElements.getPackageOf(validAnnotedClass).toString();
        String fullQualifiedClassName = validAnnotedClass.getQualifiedName().toString();
        String simpleClassName = validAnnotedClass.getSimpleName().toString();
        String newGeneratedClassName = simpleClassName + BUILDER_PREFIX;

        List<MethodSpec> genMethodsList = new ArrayList<>();

        MethodSpec buildMtd = MethodSpec.methodBuilder("build")
                .addModifiers(PUBLIC)
                .returns(TypeName.get(validAnnotedClass.asType()))
                .addStatement(String.format("return %sinstance", simpleClassName))
                .build();
        genMethodsList.add(buildMtd);

        for (Element field : validAnnotedClass.getEnclosedElements()) {
            if (field.getKind() == ElementKind.FIELD) {
                MethodSpec fldMtd = MethodSpec.methodBuilder(field.getSimpleName().toString())
                        .addModifiers(PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(field.asType()), field.getSimpleName().toString())
                                .build())
                        .addStatement(
                                String.format("%sinstance.%s=%s", simpleClassName, field.getSimpleName().toString(),
                                        field.getSimpleName().toString()))
                        .addStatement("return this")
                        .returns(ClassName.get(pkg, newGeneratedClassName))
                        .build();
                genMethodsList.add(fldMtd);
            }
        }

        FieldSpec instanceField = FieldSpec.builder(TypeName.get(validAnnotedClass.asType()), String.format("%sinstance", simpleClassName), PRIVATE)
                .initializer(String.format("new %s()", simpleClassName))
                .build();
        TypeSpec newClassSpec = TypeSpec.classBuilder(newGeneratedClassName)
                .addModifiers(PUBLIC)
                .addField(instanceField)
                .addMethods(genMethodsList)
                .build();

        JavaFile newJavaFile = JavaFile.builder(pkg, newClassSpec).build();
        newJavaFile.writeTo(System.out);
        newJavaFile.writeTo(filer);
    }
}
