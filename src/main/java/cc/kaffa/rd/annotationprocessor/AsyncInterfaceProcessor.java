package cc.kaffa.rd.annotationprocessor;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import cc.kaffa.rd.annotation.Async;

public class AsyncInterfaceProcessor extends AbstractProcessor {

	private Elements elementUtils;

	private Types typeUtils;

	private Filer filer;

	private Messager messager;

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.elementUtils = processingEnv.getElementUtils();
		this.typeUtils = processingEnv.getTypeUtils();
		this.filer = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();
	}

	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotataions = new HashSet<String>();
		annotataions.add(Async.class.getCanonicalName());
		return annotataions;
	}

	/**
	 * 1. 检查原始的接口名不能以Async结尾； 2. 对于public的方法才能生成异步方法 3. 返回值必须不能是Future 4.
	 * 方法名必须不能以Async结尾
	 * 
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// TODO Auto-generated method stub
		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Async.class)) {
			if (!checkType(annotatedElement)) {
				return false;
			}
			com.squareup.javapoet.TypeSpec.Builder typeBuilder = TypeSpec
					.interfaceBuilder(annotatedElement.getSimpleName() + "Async").addModifiers(Modifier.PUBLIC);
			for (Element element : annotatedElement.getEnclosedElements()) {
				if (element.getKind() == ElementKind.METHOD && element.getModifiers().contains(Modifier.PUBLIC)) {
					ExecutableElement exeElement = (ExecutableElement) element;
					System.out.println(exeElement.getReturnType().toString());
					com.squareup.javapoet.MethodSpec.Builder methodBuilder = MethodSpec
							.methodBuilder(exeElement.getSimpleName().toString())
							.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
					if (!exeElement.getReturnType().toString().equals("void")) {
						TypeKind returnTypeKind = exeElement.getReturnType().getKind();
						if (returnTypeKind.isPrimitive()) {
							TypeName actReturn = ClassName
									.get(typeUtils.boxedClass((PrimitiveType) exeElement.getReturnType()));
							ClassName futureType = ClassName.get("java.util.concurrent", "Future");
							methodBuilder.returns(ParameterizedTypeName.get(futureType, actReturn));
						} else {
							TypeName actReturn = ClassName.get(exeElement.getReturnType());
							ClassName futureType = ClassName.get("java.util.concurrent", "Future");
							methodBuilder.returns(ParameterizedTypeName.get(futureType, actReturn));
						}
					}
					List<? extends VariableElement> parameters = exeElement.getParameters();
					if (parameters != null) {
						for (VariableElement ve : parameters) {
							methodBuilder.addParameter(ClassName.get(ve.asType()), ve.getSimpleName().toString());
						}
					}
					typeBuilder.addMethod(methodBuilder.build());
				}
			}
			JavaFile javaFile = JavaFile.builder(getPackage(annotatedElement.asType().toString()), typeBuilder.build())
					.build();
			JavaFileObject jfo;
			try {
				jfo = filer.createSourceFile(annotatedElement.asType().toString()+"Async");
				Writer w = jfo.openWriter();
				try {
					javaFile.writeTo(w);
					w.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					w.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			

		}
		return true;
	}

	private boolean checkType(Element element) {
		if (element.getKind() != ElementKind.INTERFACE) {
			messager.printMessage(Diagnostic.Kind.ERROR,
					String.format("Async annotation only can modify the interface type, but it modify the %s type %s.",
							element.getKind().toString(), element.getSimpleName()),
					element);
			return false;
		}
		TypeElement typeAnnoElement = (TypeElement) element;
		// 检查原始的接口名不能以Async结尾
		if (typeAnnoElement.getSimpleName().toString().endsWith("Async")) {
			messager.printMessage(Diagnostic.Kind.ERROR,
					String.format("Interface %s's name can't be end with Async", typeAnnoElement.getQualifiedName()),
					element);
			return false;
		}
		return true;
	}

	private boolean checkMethod(ExecutableElement exeElement) {
		return true;
	}

	private String getPackage(String fullName) {
		if (fullName != null && !fullName.equals("")) {
			int pos = fullName.lastIndexOf(".");
			if (pos > 0) {
				return fullName.substring(0, pos);
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

}
