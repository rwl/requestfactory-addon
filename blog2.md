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