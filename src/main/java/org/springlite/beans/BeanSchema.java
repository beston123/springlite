package org.springlite.beans;


public interface BeanSchema {


    static interface ELEMENT {

        public static final String BEANS = "beans";

        public static final String BEAN = "bean";

        public static final String IMPORT = "import";

        public static final String PROPERTY = "property";

        public static final String CONSTRUCTOR_ARG = "constructor-arg";
    }

    static interface ATTRIBUTE {

        public static final String RESOURCE = "resource";

        public static final String NAME = "name";

        public static final String ID = "id";

        public static final String PARENT = "parent";

        public static final String CLASS = "class";

        public static final String ABSTRACT = "abstract";

        public static final String SCOPE = "scope";

        public static final String SINGLETON = "singleton";

        public static final String LAZY_INIT = "lazy-init";

        public static final String AUTOWIRE = "autowire";

        public static final String AUTOWIRE_CANDIDATE = "autowire-candidate";

        public static final String PRIMARY = "primary";

        public static final String DEPENDENCY_CHECK = "dependency-check";

        public static final String DEPENDS_ON = "depends-on";

        public static final String INIT_METHOD = "init-method";

        public static final String DESTROY_METHOD = "destroy-method";

        public static final String FACTORY_METHOD = "factory-method";

        public static final String FACTORY_BEAN = "factory-bean";

        public static final String INDEX = "index";

        public static final String TYPE = "type";

        public static final String REF = "ref";

        public static final String VALUE = "value";


    }

    public static final String COMMA_VALUE = ",";

    /**
     * Value of a T/F attribute that represents true.
     * Anything else represents false. Case seNsItive.
     */
    public static final String TRUE_VALUE = "true";

    public static final String FALSE_VALUE = "false";

    public static final String DEFAULT_VALUE = "default";

    public static final String AUTOWIRE_NO_VALUE = "no";

    public static final String AUTOWIRE_BY_NAME_VALUE = "byName";

    public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";

    public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";

    public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

    public static final String DEPENDENCY_CHECK_ALL_VALUE = "all";

    public static final String DEPENDENCY_CHECK_SIMPLE_VALUE = "simple";

    public static final String DEPENDENCY_CHECK_OBJECTS_VALUE = "objects";

    public static final String AUTOWIRE_CANDIDATE = "autowire-candidate";


    public static final String REF_ELEMENT = "ref";

    public static final String IDREF_ELEMENT = "idref";

    public static final String BEAN_REF = "bean";

    public static final String LOCAL_REF = "local";

    public static final String PARENT_REF = "parent";

    public static final String VALUE_ELEMENT = "value";

    public static final String NULL_ELEMENT = "null";

    public static final String ARRAY_ELEMENT = "array";

    public static final String LIST_ELEMENT = "list";

    public static final String SET_ELEMENT = "set";

    public static final String MAP_ELEMENT = "map";

    public static final String ENTRY_ELEMENT = "entry";

    public static final String KEY_ELEMENT = "key";


}
