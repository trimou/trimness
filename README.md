# Trimness

[![Travis CI Build Status](https://img.shields.io/travis/trimou/trimness/master.svg)](https://travis-ci.org/trimou/trimness)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Trimness aims to be an extensible tool to build a lightweight service for rendering templates.
It's built on many open source projects.
The fundamental ones are:

* [Trimou](http://trimou.org/) - mustache/handlebars-like templating engine
* [Weld](http://weld.cdi-spec.org) - component model, extensibility
* [Vert.x](http://vertx.io) - web server, routing, event bus

## Get started

Trimness is not yet in Maven central so you'll have to build it locally first:

> $ mvn clean install

## How does it work?

Trimness's business is to render templates.
There are two ways to send a "render request" to trimness.

1. HTTP endpoints
2. Vert.x Event Bus

### HTTP endpoints

| HTTP method | Path          | Consumes | Description |
|------------|---------------|--------------|--------------|
| POST | /render | application/json | Render request |
| GET | /result/:resultId | - | Get the result of async of render request |
| DELETE | /result/:resultId | - | Remove the result of async of render request |
| GET | /result/link/:linkId | - | Get the result for the specified link |
| GET | /monitor/health | - | Simple health-check resource |
| GET | /template/:id | - | Attempts to find the template with the given id |

#### HTTP endpoint "hello world"

Let's use `curl` to perform a very simple render request:

> curl -X POST -H "Content-Type: application/json" -d '{ "content" : "Hello {{model.name}}!", "model" : { "name" : "Foo"} }' http://localhost:8080/render

The reply will be `Hello Foo!`.
Let's analyze the request payload.
`content` property is used to specify the template for one-off rendering (TODO: It is much better to leverage the template providers and template cache).
`model` property holds the data used during rendering.
By default, the request is synchronous which means that the client is waiting for the rendered output.
If we change the payload to:

```json
{ "content" : "Hello {{model.name}}!", "model" : { "name" : "Foo"}, "async": true }
```

Then trimness replies immediately with something like:

```json
{ "time" : "2017-05-19T11:25:50.393", "resultId" : "1495185748798", "timeout" : "2017-05-19T11:30:50.393"}
```

`time` is the current server time.
`timeout` is the time after which the result might be removed automatically (it depends on the underlying result repository implementation).
The `resultId` is used to pick up the result later:

> curl http://localhost:8080/result/1495185748798

And the reply should be again `Hello Foo!`.
We can also specify the result type:

> curl http://localhost:8080/result/1495185748798?resultType=metadata

In this case, the reply would be:

```json
{ "time" : "2017-05-19T11:25:50.393", "result" : { "id" : "1495185748798", "templateId" : "oneoff_1495201157642", "output" : "Hello Foo!", "status" : "SUCCESS" }}
```

### Vert.x Event Bus

A message consumer is automatically registered for address `org.trimou.trimness.render`.
The consumer replies to the message with the result of rendering request.
The message payload should be the same as for the `/render` HTTP endpoint, except that `async` option is ignored.

### Packaging and Deployment

TODO