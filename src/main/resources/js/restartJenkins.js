AJS.toInit(function(){
    AJS.$('#restartJenkins').on('click', function(evt) {
       evt.preventDefault();
    AJS.$.get(AJS.contextPath()+ "/rest/scriptrunner/latest/custom/restartJenkinsFailedStage?issueId="+JIRA.Issue.getIssueId()+"", function(data){
       require(['aui/flag'], function(flag) {
           var myFla = flag({
               type: data.type,
               title: 'Restart Jenkins',
               close:'auto',
               body: data.message
                });
            });
        });

    });
});