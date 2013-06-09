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

        roo> system service --name LAYOUT_INFLATER --fieldName inflater

## Other commands

        roo> permission --name FLASHLIGHT
        
        
# RequestFactory add-on for Spring Roo

In the [keynote](http://www.youtube.com/watch?v=a46hJYtsP-8) speech at [Google I/O](http://code.google.com/io) 2010 SpringSource's [Ben Alex](http://www.springone2gx.com/conference/speaker/ben_alex) presented [Spring Roo](http://www.springsource.org/spring-roo) its new Google WebToolkit (GWT) [add-on](http://static.springsource.org/spring-roo/reference/html/base-gwt.html). This article describes a fork of the GWT add-on that provides Android client side scaffolding that also uses the [RequestFactory](https://developers.google.com/web-toolkit/doc/latest/DevGuideRequestFactory) protocol. The templates for the GWT client side scaffolding have been modified to use [GWT Bootstrap](http://gwtbootstrap.github.com/) and [mgwt](http://www.m-gwt.com/).

## Getting started

The add-on provides a configuration for Roo's [tailor add-on](http://static.springsource.org/spring-roo/reference/html/base-tailor.html). This customizes the shell for use with projects created using the add-on.

        roo> tailor activate --name requestfactory

With the tailor configuration activated the [project](http://static.springsource.org/spring-roo/reference/html/beginning.html) command will create a multi-module Maven project with the structure:

+ `client`
    - `android`
    - `gwt`
+ `shared`
+ `server`

The `shared` module has no runtime dependencies upon the `server` or the `client` module. The `server` module will be focused for all [JPA](http://static.springsource.org/spring-roo/reference/html/base-persistence.html) and [MongoDB](http://static.springsource.org/spring-roo/reference/html/base-layers.html) commands. The `shared` module is focussed for all [enum](http://static.springsource.org/spring-roo/reference/html/command-index.html#command-index-enum-constant) commands. All commands beginning `web requestfactory` no longer require that prefix when the `requestfactory` tailoring is active.

## Server side

[Active Record](http://static.springsource.org/spring-roo/reference/html/base-layers.html)-style entities may be used with JPA. [Service and repository](http://blog.springsource.org/2011/09/14/new-application-layering-and-persistence-choices-in-spring-roo/) layers are required for Mongo and may also be used with JPA. An entity can be identified for use with the RequestFactory add-on using the `scaffold entity` command. The `primary` and `secondary` options can be used to indicate the fields that best describe the entity (this metadata may be used when generating user interfaces).

        roo> scaffold entity

Since Roo can not remove of modify fields directly once they have been added to an entity the tailored add-on effectively overrides the [field commands](http://static.springsource.org/spring-roo/reference/html/base-persistence.html#d4e1640) to provide these options:

+ `exclude` - field is to be excluded from the proxy
+ `readOnly` - field is to be read only in the proxy
+ `invisible` - field is not to be included in views of the proxy
+ `uneditable` - field is not to be included when editing the proxy
+ `helpText` - a short string describing the field (used in tooltips etc.)

The `field string` command is extended to provide `password` and `textArea` options that add an annotation that can influrnce the type of widget used on the client-side.

        roo> field string --fieldName password --password --invisible

The `reference` command has an `unowned` option that marks the field with the [Unowned](http://code.google.com/p/datanucleus-appengine/source/browse/trunk/src/com/google/appengine/datanucleus/annotations/Unowned.java) annotation from Google AppEngine (GAE) to support [unowned](https://developers.google.com/appengine/docs/java/datastore/jdo/relationships#Unowned_Relationships) one-to-one and many-to-one relationships. The add-on also provides a `field list` command that closely resembles the existing `field set` command and provides support for [owned](https://developers.google.com/appengine/docs/java/datastore/jdo/relationships#Owned_One_to_Many_Relationships) one-to-many relationships when using GAE.

## Shared artifacts

Some classes are required by both the `client` module and the `server` module and some classes are used by multiple client-side implementations.
As in the [GWT add-on](http://static.springsource.org/spring-roo/reference/html/base-gwt.html), RequestFactory [Proxy](http://google-web-toolkit.googlecode.com/svn/javadoc/2.1/com/google/gwt/requestfactory/shared/EntityProxy.html) and [Request](http://google-web-toolkit.googlecode.com/svn/javadoc/2.1/com/google/gwt/requestfactory/shared/Request.html) classes can be generated for each of the entities.

        roo> proxy request all

The application scaffold that may be shared between the clients can be generated using this command:

        roo> scaffold all

> Until [ROO-2988](https://jira.springsource.org/browse/ROO-2988) is resolved a dependency on the `server` module will have to be removed from the `shared` module to ensure decoupling.

        roo> dependency remove --groupId com.example.drooid.server --artifactId drooid-server --version 0.1.0.BUILD-SNAPSHOT

## Client side

To generate scaffolding for a GWT client application use the command:

        roo> gwt scaffold all

To generate scaffolding for an Android client application use the command:

        roo> android scaffold all       
