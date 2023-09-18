AJS.toInit(function(){
    AJS.$('#addTobeDeletedLabel').on('click', function(evt) {
       evt.preventDefault();
    AJS.$.get(AJS.contextPath()+ "/rest/scriptrunner/latest/custom/addTobeDeletedLabel?issueId="+JIRA.Issue.getIssueId()+"", function(data){
       require(['aui/flag'], function(flag) {
           var myFla = flag({
               type: data.type,
               title: 'Move Issue to Trash',
               close:'auto',
               body: data.message
                });
            });
        });

    });
});