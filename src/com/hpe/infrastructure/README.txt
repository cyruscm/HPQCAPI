This infrastructure is based off HP ALM's rest tutorial for 12.01, seen here
http://whqwalm02.tyson.com/qcbin/Help/doc_library/api_refs/REST/webframe.html#CSHID=General%2FFiltering.html|StartTopic=Content%2FGeneral%2FFiltering.html
if that link is no longer valid, it can also be seen here but with incorrect information (for version 12.5) can be seen here
http://alm-help.saas.hpe.com/de/12.50/api_refs/REST_TECH_PREVIEW/ALM_REST_API_TP.html

I do not suggest making changes to any of the infrastructure as all of com.tyson.hpqcapi is 
built as a wrapper around it. One manual change was done, which is content-length injection 
in the RestConnector due to Content-Length 411 Requirements in ALM while the utility that the 
rest connector is wrapped on does not pass content-length in null cases.