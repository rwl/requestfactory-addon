# Drooid - Android add-on for Spring Roo

This article describes an add-on for [Spring Roo](http://www.springsource.org/spring-roo) that can be used to accelerate development of [Android](http://developer.android.com/) applications. The add-on extends the Roo shell with an `apk` Maven module packaing type and commands for common tasks including:

* Creating activities and fragments,
* Creating layouts and adding views to them,
* Adding resources, system services and permissions and
* Binding views, resources and system services to fields.

The add-on provides annotations that are used by Roo to generate [AspectJ](http://www.eclipse.org/aspectj/) Inter-Type Declarations (ITDs). These separate the concern of initialising views, resources, services etc. from adding functionality to an application. The technique resembles that used in the [AndroidAnnotations](http://androidannotations.org/) project. The Roo annotations [can be removed](http://static.springsource.org/spring-roo/reference/html/removing.html) and the ITD code pushed into the standard `.java` files if preferred.

## Getting started

The add-on is provided as two JARs. They are standard [OSGI](http://felix.apache.org/) bundles and can be dropped into the `bundle` sub-directory of a Roo installation.

The `ànnotations` JAR does not have any dependencies upon the [Spring Framework](http://www.springsource.org/spring-framework). It can be added as a dependency to an existing Android project to provide the annotations used by Roo.

An Android project can be created using the normal [project](http://static.springsource.org/spring-roo/reference/html/beginning.html) command with `apk` packaging specified:

        roo> project --topLevelPackage org.roodroid --projectName roodroid --packaging APK

Alternatively, the add-on provides a configuration of the [tailor add-on](http://static.springsource.org/spring-roo/reference/html/base-tailor.html). If activated `apk` packaging is used by default.

        roo> tailor activate --name android
        roo> project --topLevelPackage org.roodroid --projectName roodroid

Additionally, when the tailor configuration is activated the commands provided by the add-on need not begin with `android`.

## Activities and Fragments

The `activity` command creates a new sub-class of [Activity](http://developer.android.com/reference/android/app/Activity.html) and adds an `<activity/>` element to the [AndroidManifest.xml](http://developer.android.com/guide/topics/manifest/manifest-intro.html) file.

        roo> activity --class ~.MainActivity --launcher --layout main --fullscreen

If specified, the `launcher` option designates the activity as being the [main entry point](http://developer.android.com/training/basics/activity-lifecycle/starting.html#launching-activity) to the app's user interface. The `layout` option can be used to specify the name of a [layout resource](http://developer.android.com/guide/topics/resources/layout-resource.html). If the resource does not exist a simple layout will be created. Roo will generate an ITD implementing the `onCreate` method and pass the id of the resource to `setContentView`. The `noTitle` option makes requesting a window feature with no title convenient. Similarly, the `fullscreen` option sets flags that configure the window to use the entire screen.

        roo> fragment --class ~.MyFragment --layout fragment_layout --support

The `fragment` command creates a new sub-class of [Fragment](http://developer.android.com/reference/android/app/Fragment.html). The `support` option makes Roo use the [Fragment](http://developer.android.com/reference/android/support/v4/app/Fragment.html) class from the [Android Support Library](http://developer.android.com/training/basics/fragments/support-lib.html) and adds a dependency to the Maven POM if necessary. The `layout` option can specify a layout resource to be bound to a `contentView` field in the generated `onCreateView` method.

## Field commands

The `view` command creates a [View](http://developer.android.com/reference/android/view/View.html) and binds it to a field on the specified `type`. If the `type` has a layout the view will be added to it. The `view` option can be either the fully-qualified type name or the simple name, where the package is assumed to be `android.widget`.

        roo> view --type ~.MainActivity --fieldName textView --view TextView

The `resource` command can be used to add [resources](http://developer.android.com/guide/topics/resources/available-resources.html) and bind them to field values.

        roo> resource string --fieldName message --value "foo bar"

The `service` command binds a [system service](http://developer.android.com/reference/android/content/Context.html#getSystemService%28java.lang.String%29) the a field on the given `type`.

        roo> service --name LAYOUT_INFLATER --fieldName inflater

## Other commands

        roo> permission --name FLASHLIGHT