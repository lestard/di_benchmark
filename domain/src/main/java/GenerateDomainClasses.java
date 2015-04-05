import com.sun.codemodel.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenerateDomainClasses {

    public static final int NUMBER_OF_CLASSES_IN_EACH_CATEGORY = 10;

    public static final String PACKAGE_ROOT = "domain.generated";

    public static final String PACKAGE_LEAFS = PACKAGE_ROOT + ".leafs";
    public static final String PACKAGE_SINGLE_DEPENDENCY= PACKAGE_ROOT + ".singledep";
    public static final String PACKAGE_INTERFACES = PACKAGE_ROOT + ".interfaces";
    public static final String PACKAGE_TWO_DEPS =  PACKAGE_ROOT + ".twodeps";
    public static final String PACKAGE_AGGREGATOR =  PACKAGE_ROOT + ".aggregator";


    private List<JDefinedClass> leafs = new ArrayList<>();
    private List<JDefinedClass> interfaces = new ArrayList<>();
    private List<JDefinedClass> singleDeps = new ArrayList<>();
    private List<JDefinedClass> twoDeps = new ArrayList<>();
    private List<JDefinedClass> aggregators = new ArrayList<>();



	public static void main(String[] args) throws Exception{
        new GenerateDomainClasses().createWithCodeModel();
	}

    private void createWithCodeModel() throws Exception {
        JCodeModel codeModel = new JCodeModel();

        createInterfaces(codeModel);

        createLeafClasses(codeModel);

        createSingleDependencyClasses(codeModel);

        createTwoDependencyClasses(codeModel);

        createAggregators(codeModel);


        createRoot(codeModel);


        File file = new File("domain/src/main/java/");
        file.mkdirs();

        System.out.println(">" + file.getAbsolutePath());

        codeModel.build(file);
    }

    private void createRoot(JCodeModel codeModel) throws Exception {
        final JPackage pack = codeModel._package(PACKAGE_ROOT);

        final JDefinedClass root = pack._class("Root");
        final JMethod constructor = root.constructor(JMod.PUBLIC);
        constructor.annotate(Inject.class);

        createField(root, constructor, aggregators.get(aggregators.size()-1), "lastAggregator");

    }

    private void createAggregators(JCodeModel codeModel) throws Exception{
        final JPackage pack = codeModel._package(PACKAGE_AGGREGATOR);

        List<JDefinedClass> firstLevelAggregators = new ArrayList<>();

        for(int i=0 ; i<NUMBER_OF_CLASSES_IN_EACH_CATEGORY ; i++){
            final JDefinedClass aClass = pack._class("Aggregator" + i);
            final JMethod constructor = aClass.constructor(JMod.PUBLIC);
            constructor.annotate(Inject.class);

            createField(aClass, constructor, leafs.get(i), "Leaf");
            createField(aClass, constructor, interfaces.get(i), "Inter");
            createField(aClass, constructor, singleDeps.get(i), "SingleDep");
            createField(aClass, constructor, twoDeps.get(i), "TwoDep");

            firstLevelAggregators.add(aClass);
        }

        aggregators.addAll(firstLevelAggregators);

        _aggregate(firstLevelAggregators, pack, 0);
    }

    private void _aggregate(List<JDefinedClass> rest, JPackage pack, int lastIndex) throws Exception {
        int subAggregatorIndex = 0;

        List<JDefinedClass> subAggregators = new ArrayList<>();

        List<JDefinedClass> tmp = new ArrayList<>(rest);

        JDefinedClass lastSubAggregator;

        while(!tmp.isEmpty()) {
            final int i = Math.min(5, tmp.size());
            final List<JDefinedClass> sublist = tmp.subList(0, i);


            final JDefinedClass aClass = pack._class("SubAggregator" + lastIndex + "_" + subAggregatorIndex);
            lastSubAggregator = aClass;
            subAggregatorIndex++;

            final JMethod constructor = aClass.constructor(JMod.PUBLIC);
            constructor.annotate(Inject.class);

            for (int j = 0; j < sublist.size(); j++) {
                JDefinedClass definedClass = sublist.get(j);
                createField(aClass, constructor, definedClass, "Aggregator" + j);
            }
            aggregators.add(aClass);

            subAggregators.add(aClass);

            tmp.removeAll(sublist);
        }

        if(subAggregators.size() > 1) {
            _aggregate(subAggregators, pack, lastIndex + 1);
        }
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

            createField(aClass, constructor, leafs.get(i), "Leaf");
            createField(aClass, constructor, interfaces.get(i), "Inter");

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

    private void createField(JDefinedClass aClass, JMethod constructor, JDefinedClass leafType, String fieldName) {
        final JFieldVar field1 = aClass.field(JMod.PRIVATE, leafType, fieldName.toLowerCase());
        final JVar param1 = constructor.param(leafType, "_" + fieldName.toLowerCase());
        constructor.body().assign(field1, param1);

        final JMethod getter = aClass.method(JMod.PUBLIC, leafType, "get" + fieldName);
        getter.body()._return(field1);
    }


    private void createSingleDependencyClasses(JCodeModel codeModel) throws Exception {
        final JPackage pack = codeModel._package(PACKAGE_SINGLE_DEPENDENCY);

        for(int i=0 ; i< NUMBER_OF_CLASSES_IN_EACH_CATEGORY ; i++){
            final JDefinedClass aClass = pack._class("SingleDep" + i);
            final JMethod constructor = aClass.constructor(JMod.PUBLIC);
            constructor.annotate(Inject.class);

            createField(aClass, constructor, leafs.get(i), "Leaf");

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
