var serviceTable= new SCFile("device");
var serviceQuery = "logical.name = \""+ record.affected_item+ "\"";
var service_rc=serviceTable.doSelect(serviceQuery);
record.elm_jira_service_key = serviceTable.elm_jira_service_key;

var JSON = system.library.JSON.json();

var headers = new Array();
// fill-in headers
headers.push(new Header( "Content-Type", "application/json" ))
headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
var respHeaders = new Object();

var description  = system.library.JSON2.toJSON(record.description.toArray().join('\n'))

var data = ' { "fields": { "project": { "key": "'+record.elm_jira_service_key+'" }, "summary": "'+record.brief_description+'", "description": '+description+', "issuetype": { "name": "Problem" }, "priority":{"id": "'+record.priority_code+'" }, "customfield_11101" : "'+record.id+'" },"customfield_15502": {  "value": "Technical Enhancement"} }'

var url = 'http://10.33.6.16:8080/rest/api/2/issue';
var response = doHTTPRequest( "POST", url, headers, data , 10, 10, null, respHeaders );
eval('var responseObject =' + response);
print(responseObject.key);
record.elm_jazz_defect_number = responseObject.key;