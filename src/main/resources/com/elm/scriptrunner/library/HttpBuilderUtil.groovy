package com.elm.scriptrunner.library

import groovy.util.logging.Log4j
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.log4j.Level
import org.apache.log4j.Logger


@Log4j
class HttpBuilderUtil {

    static def handler(RESTClient http) {
        http.handler.failure = { resp, reader ->
            [response: resp.statusLine.statusCode, reader: reader]
        }
        http.handler.success = { resp, reader ->
            [response: resp.statusLine.statusCode, reader: reader]
        }
    }

    static def getUtil(String url, String path, String userName, String password, String contentType) {
        try {
            def client = new RESTClient(url)
            def authString = "${userName}:${password}".getBytes().encodeBase64().toString()
            client.setHeaders([Authorization: "Basic ${authString}"])
            handler(client)
            client.get(path: path, requestContentType: contentType as ContentType)
        } catch (HttpResponseException e) {
            return [response: e]
        } catch (Exception e) {
            return [response: e]
        }
    }

    static def postUtil(String url, String path, String userName, String password, def body, String contentType) {
        try {
            def client = new RESTClient(url)
            def authString = "${userName}:${password}".getBytes().encodeBase64().toString()
            client.setHeaders([Authorization: "Basic ${authString}"])
            handler(client)
            def resp = client.post(path: path, body: body, requestContentType: contentType as ContentType)
        } catch (HttpResponseException e) {
            return [response: e]
        } catch (Exception e) {
            return [response: e]
        }
    }

    static def putUtil(String url, String path, String userName, String password, def body, String contentType) {
        try {
            def client = new RESTClient(url)
            def authString = "${userName}:${password}".getBytes().encodeBase64().toString()
            client.setHeaders([Authorization: "Basic ${authString}"])
            handler(client)
            def resp = client.put(path: path, body: body, requestContentType: contentType as ContentType)
        } catch (HttpResponseException e) {
            return [response: e]
        } catch (Exception e) {
            return [response: e]
        }
    }

    static def putUtil(String url, String path, String userName, String query, String password, def body, String contentType) {
        try {
            def client = new RESTClient(url)
            def authString = "${userName}:${password}".getBytes().encodeBase64().toString()
            client.setHeaders([Authorization: "Basic ${authString}"])
            handler(client)
            def resp = client.put(path: path, body: body, query: query, requestContentType: contentType as ContentType)
        } catch (HttpResponseException e) {
            return [response: e]
        } catch (Exception e) {
            return [response: e]
        }
    }

    static def postUtil(String url, String path, String userName, String query, String password, String body, String contentType) {
        try {
            def client = new RESTClient(url)
            def authString = "${userName}:${password}".getBytes().encodeBase64().toString()
            client.setHeaders([Authorization: "Basic ${authString}"])
            handler(client)
            def resp = client.post(path: path, body: body, query: query, requestContentType: contentType as ContentType)
        } catch (HttpResponseException e) {
            return [response: e]
        } catch (Exception e) {
            return [response: e]
        }
    }

}