package com.elm.scriptrunner.library

import com.opensymphony.workflow.InvalidInputException
import groovy.util.logging.Log4j
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import  groovyx.net.http.ContentType
import kong.unirest.UnirestException

@Log4j
class HttpRestUtil {

    static String smAuthString  = getAuthString(Constants.smUsername,Constants.smPassword)
    static String JAuthString  = getAuthString(Constants.JenkinsUsername,Constants.JenkinsPassword)
    static String CrowdAuthString  = getAuthString(Constants.crowdUsername,Constants.crowdPassword)
    static String smTestAuthString  = getAuthString(Constants.smTestUsername,Constants.smTestPassword)

    static def doQueryGet(String url, Map query, def authString, def contentType) {
        HttpResponse jsonResponse =
            Unirest.get(url)
                .header('Authorization', "Basic ${authString}")
                .header('Content-Type', contentType)
                .queryString(query)
                .asJson()
                .ifSuccess { response -> response.body}
                .ifFailure { response ->
                    log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                    response.getParsingError().ifPresent {
                        log.warn("Parsing Exception: " + it)
                        log.warn("Original body: " + it.getMessage())
                    }
                }
        return jsonResponse
    }

    static def doQueryPost(String url, Map query, String body, def authString, def contentType) {
        HttpResponse jsonResponse =
            Unirest.post(url)
                .header('Authorization', "Basic ${authString}")
                .header('Content-Type', contentType)
                .queryString(query)
                .body(body)
                .asJson()
                .ifSuccess { response -> response.body }
                .ifFailure { response ->
                    log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                    response.getParsingError().ifPresent {
                        log.warn("Parsing Exception: " + it)
                        log.warn("Original body: " + it.getMessage())
                    }
                }
        return jsonResponse
    }

    static def doQueryPut(String url, Map query, String body, def authString, def contentType) {
        HttpResponse jsonResponse =
            Unirest.post(url)
                .header('Authorization', "Basic ${authString}")
                .header('Content-Type', contentType)
                .queryString(query)
                .body(body)
                .asJson()
                .ifSuccess { response -> response.body}
                .ifFailure { response ->
                    log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                    response.getParsingError().ifPresent {
                        log.warn("Parsing Exception: " + it)
                        log.warn("Original body: " + it.getMessage())
                    }
                }
        return jsonResponse
    }

    static def JFieldPost( String path ,Map fields) {
        HttpResponse jsonResponse =
            Unirest.post(Constants.JenkinsUrl+path)
                .header('Authorization', "Basic ${JAuthString}")
                .fields(fields)
                .asJson()
        return jsonResponse
    }

    static def SMGet(String path) {

        def jsonResponse =
            Unirest.get(Constants.smUrl+path)
                .header('Authorization', "Basic ${smAuthString}")
                .header('Content-Type', ContentType.JSON as String)
                .asJson()
                .ifSuccess { it.body.object}
                .ifFailure { response ->
                    log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                    response.getParsingError().ifPresent {
                        log.warn("Parsing Exception: " + it)
                        log.warn("Original body: " + it.getOriginalBody())
                    }
                }
        return jsonResponse
    }

    static def SMPost(String path, String body) {
        HttpResponse jsonResponse =
            Unirest.post(Constants.smUrl+path)
                .header('Authorization', "Basic ${smAuthString}")
                .header('Content-Type', ContentType.JSON as String)
                .body(body)
                .asJson()
                .ifSuccess { it.body.object}
                .ifFailure { response ->
                    log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.body.object.Messages)
                    response.getParsingError().ifPresent {
                        log.warn("Parsing Exception: " + it)
                        log.warn("Original body: " + it.getOriginalBody())
                    }
                }
        return jsonResponse
    }

    static def SMPut(String path, String body) {
        HttpResponse jsonResponse =
            Unirest.put(Constants.smUrl+path)
                .header('Authorization', "Basic ${smAuthString}")
                .header('Content-Type', ContentType.JSON as String)
                .body(body)
                .asJson()
                .ifSuccess { it.body.object}
                .ifFailure { response ->
                    log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.body.object.Messages)
                    response.getParsingError().ifPresent {
                        log.warn("Parsing Exception: " + it)
                        log.warn("Original body: " + it.getOriginalBody())
                    }
                }
        return jsonResponse
    }

    static def SMPostAttachment(String path, String fileName, String filePath){
        HttpResponse jsonResponse =
            Unirest.post(Constants.smUrl+path)
                    .header('Authorization', "Basic ${smAuthString}")
                    .header('Content-Disposition', "attachment; filename=${fileName}")
                    .header('Content-Type', "application/octet-stream")
//                    .field("${fileName}",new File("${filePath}"))
                    .body(new File("${filePath}"))
                    .asJson()
                    .ifFailure { response ->
                        log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.body)
                        response.getParsingError().ifPresent {
                            log.warn("Parsing Exception: " + it)
                            log.warn("Original body: " + it.getOriginalBody())
                    }
                }
        return jsonResponse
    }

    static def SMGetAttachment(String path) {
        HttpResponse jsonResponse =
            Unirest.get(Constants.smUrl+path)
                .header('Authorization', "Basic ${smAuthString}")
                .asJson()
        return jsonResponse
    }

    static def JGet(String path) {
        def jsonResponse =
            Unirest.get(Constants.JenkinsUrl+path)
                .header('Authorization', "Basic ${JAuthString}")
                .asJson()
        return jsonResponse
    }

    static def JPost(String path, def body) {
        try {
            HttpResponse jsonResponse =
                Unirest.post(Constants.JenkinsUrl + path)
                    .header('Authorization', "Basic ${JAuthString}")
                    .header('Content-Type', ContentType.URLENC as String)
                    .queryString(body)
                    .asJson()
                    .ifSuccess { it?.body?.object }
                    .ifFailure { response ->
                        log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                        response.getParsingError().ifPresent {
                            log.warn("Parsing Exception: " + it)
                            log.warn("Original body: " + it?.getOriginalBody())
                        }
                    }
            return jsonResponse
        }catch(UnirestException e){
            throw new InvalidInputException("Some error occurred while on approval stage, please try again if error still exist contact techsupport@elm.sa"+e.message)
        }
    }

    static def JPost(String path) {
        try {
            HttpResponse jsonResponse =
                Unirest.post(Constants.JenkinsUrl + path)
                    .header('Authorization', "Basic ${JAuthString}")
                    .header('Content-Type', ContentType.URLENC as String)
                    .asJson()
                    .ifSuccess { it?.body?.object }
                    .ifFailure { response ->
                        log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                        response.getParsingError().ifPresent {
                            log.warn("Parsing Exception: " + it)
                            log.warn("Original body: " + it?.getOriginalBody())
                        }
                    }
            return jsonResponse
        }catch(UnirestException e){
            throw new InvalidInputException("Some error occurred while on approval stage, please try again if error still exist contact techsupport@elm.sa"+e.message)
        }
    }

    static def getAuthString(String userName, String password) {
        def authString = "${userName}:${password}".getBytes().encodeBase64().toString()
        return authString
    }

    static def crowdRestCall( def query, String body) {
        HttpResponse jsonResponse =
            Unirest.post(Constants.crowdURL+Constants.crowdRestPath)
                .header('Authorization', "Basic ${CrowdAuthString}")
                .header('Content-Type',ContentType.JSON as String)
                .queryString(query)
                .body(body)
                .asJson()
                .ifSuccess { it.body.object}
                .ifFailure { response ->
                    log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                    response.getParsingError().ifPresent {
                        log.warn("Parsing Exception: " + it)
                        log.warn("Original body: " + it.getOriginalBody())
                    }
                }
        return jsonResponse
    }

}
