package tsukka.secure;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.*;
import java.security.cert.Extension;
import java.util.Objects;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_9)
@SupportedAnnotationTypes("*")
public class Processor extends AbstractProcessor {
    private Trees trees;
    private Messager messager;
    private Filer filer;
    @Override
    public void init(ProcessingEnvironment procEnv) {
        messager=procEnv.getMessager();
        filer=procEnv.getFiler();
        trees = Trees.instance(procEnv);

    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getRootElements().forEach(this::processElement);
        return false;
    }
    private void processElement(Element el){
        CompilationUnitTree cu=toUnit(el);
        try (InputStream in=cu.getSourceFile().openInputStream();OutputStream out=filer.createResource(StandardLocation.CLASS_OUTPUT,"",((QualifiedNameable)el).getQualifiedName().toString()+"."+System.currentTimeMillis()+".bup").openOutputStream()){
            in.transferTo(out);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.toString(),el);
        }
        try (PrintWriter w = new PrintWriter(new BufferedWriter(cu.getSourceFile().openWriter()))){
            //messager.printMessage(Diagnostic.Kind.NOTE, Objects.toString(w));
            ExpressionTree pname=cu.getPackageName();
            if(Objects.nonNull(pname)){
                w.println("package "+pname.toString()+";");
            }
            w.println("public class "+el.getSimpleName()+"{");
            w.println("  /*");
            w.println("   * No code is the best way to write secure and reliable applications. Write nothing; deploy nowhere.");
            w.println("   * https://github.com/kelseyhightower/nocode");
            w.println("   */");
            w.println("}");
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.toString(),el);
        }

    }
    private CompilationUnitTree toUnit(Element el) {
        TreePath path = trees.getPath(el);
        return path.getCompilationUnit();
    }
}
