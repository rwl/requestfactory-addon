# RequestFactory add-on for Spring Roo

In the [keynote](http://www.youtube.com/watch?v=a46hJYtsP-8) speech at [Google I/O](http://code.google.com/io) 2010 SpringSource's [Ben Alex](http://www.springone2gx.com/conference/speaker/ben_alex) presented [Spring Roo](http://www.springsource.org/spring-roo) its new [GWT add-on](http://static.springsource.org/spring-roo/reference/html/base-gwt.html). This article describes a fork of the GWT add-on that provides Android client side scaffolding which also uses the [RequestFactory](https://developers.google.com/web-toolkit/doc/latest/DevGuideRequestFactory) protocol. The templates for the GWT client side scaffolding have been modified to use [GWT Bootstrap](http://gwtbootstrap.github.com/) and [mgwt](http://www.m-gwt.com/).

## Getting started

The add-on provides a configuration for Roo's [tailor add-on](http://static.springsource.org/spring-roo/reference/html/base-tailor.html). This customizes the shell for use projects created with the add-on.

        roo> tailor activate --name requestfactory
