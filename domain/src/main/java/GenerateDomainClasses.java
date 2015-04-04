import com.sun.codemodel.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenerateDomainClasses {

    public static final int NUMBER_OF_CLASSES_IN_EACH_CATEGORY = 10;

    public static final String PACKAGE_LEAFS = "leafs";
    public static final String PACKAGE_SINGLE_DEPENDENCY= "singledep";
    public static final String PACKAGE_INTERFACES = "interfaces";
    public static final String PACKAGE_TWO_DEPS = "twodeps";

    private List<JDefinedClass> leafs = new ArrayList<>();
    private List<JDefinedClass> interfaces = new ArrayList<>();
    private List<JDefinedClass> singleDeps = new ArrayList<>();
    private List<JDefinedClass> twoDeps = new ArrayList<>();



	public static void main(String[] args) throws Exception{
        new GenerateDomainClasses().createWithCodeModel();
	}

    private void createWithCodeModel() throws Exception {
        JCodeModel codeModel = new JCodeModel();

        createInterfaces(codeModel);

        createLeafClasses(codeModel);

        createSingleDependencyClasses(codeModel);

        createTwoDependencyClasses(codeModel);


        File file = new File("domain/build/_classes");
        file.mkdirs();

        System.out.println(">" + file.getAbsolutePath());

        codeModel.build(file);
    }

    private void createInterfaces(JCodeModel codeModel) throws Exception {
        final JPackage pack = codeModel._package(PACKAGE_INTERFACES);

        for(int i=0 ; i<NUMBER_OF_CLASSES_IN_EACH_CATEGORY ; i++){
            final JDefinedClass anInterface = pack._interface("Interface" + i);

            interfaces.add(anInterface);
        }
    }


    private void createTwoDependencyClasses(JCodeModel codeModel) throws Exception {
        final JPackage pack = codeModel._package(PACKAGE_TWO_DEPS);

        for(int i=0 ; i< NUMBER_OF_CLASSES_IN_EACH_CATEGORY ; i++){
            final JDefinedClass aClass = pack._class("TwoDep" + i);
            final JMethod constructor = aClass.constructor(JMod.PUBLIC);
            constructor.annotate(Inject.class);


            final JFieldVar field1 = aClass.field(JMod.PRIVATE, leafs.get(i), "leaf");
            final JVar param1 = constructor.param(leafs.get(i), "_leaf");
            constructor.body().assign(field1, param1);

            final JFieldVar field2 = aClass.field(JMod.PRIVATE, interfaces.get(i), "inter");
            final JVar param2 = constructor.param(interfaces.get(i), "_inter");
            constructor.body().assign(field2, param2);

            if(i % 8 == 0) {
                aClass.annotate(Singleton.class);
            }


            if(i % 13 == 0) {
                final JDefinedClass anInterface = interfaces.get(i);

                aClass._implements(anInterface);
            }

            twoDeps.add(aClass);
        }
    }


    private void createSingleDependencyClasses(JCodeModel codeModel) throws Exception {
        final JPackage pack = codeModel._package(PACKAGE_SINGLE_DEPENDENCY);

        for(int i=0 ; i< NUMBER_OF_CLASSES_IN_EACH_CATEGORY ; i++){
            final JDefinedClass aClass = pack._class("SingleDep" + i);
            final JFieldVar field = aClass.field(JMod.PRIVATE, leafs.get(i), "leaf");

            final JMethod constructor = aClass.constructor(JMod.PUBLIC);
            constructor.annotate(Inject.class);
            final JVar param = constructor.param(leafs.get(i), "_leaf");
            constructor.body().assign(field, param);

            // every third class will be a singleton
            if(i % 3 == 0) {
                aClass.annotate(Singleton.class);
            }


            if(i % 4 == 0) {
                final JDefinedClass anInterface = interfaces.get(i);

                aClass._implements(anInterface);
            }

            singleDeps.add(aClass);
        }
    }

    private  void createLeafClasses(JCodeModel codeModel) throws Exception{

        final JPackage leafPackage = codeModel._package(PACKAGE_LEAFS);

        for(int i=0 ; i < NUMBER_OF_CLASSES_IN_EACH_CATEGORY ; i++){
            final JDefinedClass aClass = leafPackage._class("Leaf" + i);


            // every third class will be a singleton
            if(i % 3 == 0) {
                aClass.annotate(Singleton.class);
            }

            if(i % 7 == 0) {
                final JDefinedClass anInterface = interfaces.get(i);

                aClass._implements(anInterface);
            }



            leafs.add(aClass);
        }
    }
}
