package com.qainfotech.ta.xrayrunner;

import com.qainfotech.ta.testrailrunner.*;
import com.qainfotech.ta.testrailrunner.googlesheetreader.GoogleSheet;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import java.net.URLEncoder;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Call test rail api with proper credentials and params
 * 
 * @author Ramandeep <ramandeepsingh@qainfotch.com>
 */
public class XRayApiClient {
    Map<String, String> configuration;
    Integer runId;
    String projectId;
    String testDataFileId;
    String toTestStatus;
    String failureTransitionId;
    String successTransitionId;
    
    /**
     * 
     * @param config 
     */
    public XRayApiClient(Map config){
        configuration = new HashMap();
        configuration.put("username", (String)config.get("username"));
        configuration.put("password", (String)config.get("password"));
        configuration.put("xray_url", (String)config.get("xray_url"));
    }
    
    /**
     * 
     * @throws IOException 
     */
    public XRayApiClient() throws IOException{
        configuration = new HashMap();
        Properties prop = new Properties();
        InputStream in = XRayApiClient.class.getResourceAsStream("/jira_settings.properties");
        prop.load(in);
        in.close();
        configuration.put("username", (String)prop.get("username"));
        configuration.put("password", (String)prop.get("password"));
        configuration.put("jira_url", (String)prop.get("url"));
        configuration.put("jenkins_job_url", (String)prop.get("jenkins_job_url"));
        
        /** read runId and projectId **/
        projectId = (String)prop.get("projectId");
        toTestStatus = "TEST";
        if(prop.containsKey("qa_swimlane_status")){
            toTestStatus = (String)prop.get("qa_swimlane_status");
        }
        failureTransitionId=(String)prop.get("failure_transition_id");
        successTransitionId=(String)prop.get("success_transition_id");
//        runId = new Integer(testRun.getProperty("runId"));
//        testDataFileId = testRun.getProperty("testDataFile");
//        if(System.getProperty("testRunId")!=null){
//            runId = new Integer(System.getProperty("testRunId"));
//        }
    }
    
    private HttpResponse<JsonNode> get(String url) throws UnirestException, TestRailApiClientException, IOException{
        HttpResponse<JsonNode> response = Unirest.get(configuration.get("jira_url") + url)
                .header("Content-Type", "application/json")
                .basicAuth(configuration.get("username"), configuration.get("password"))
                .asJson();
        if(response.getStatus() != 200){
            throw new TestRailApiClientException("Incorrect response code: " 
                    + Integer.toString(response.getStatus()) + ", message: " 
                    + response.getStatusText() + ", body: " + isToString(response.getRawBody()));
        }
        return response;
    }
    
    private HttpResponse<JsonNode> post(String url, Map postData) throws UnirestException, TestRailApiClientException, IOException{
        HttpResponse<JsonNode> response = Unirest.post(configuration.get("jira_url") + url)
                .header("Content-Type", "application/json")
                .basicAuth(configuration.get("username"), configuration.get("password"))
                .body(new JSONObject(postData))                
                .asJson();
        if(response.getStatus() == 200){
        }else if(response.getStatus() == 204){
        }else if(response.getStatus() == 201){
        }else{
            throw new TestRailApiClientException("Incorrect response code: " 
                    + Integer.toString(response.getStatus()) + ", message: " 
                    + response.getStatusText() + ", body: " + isToString(response.getRawBody()));
        }
        return response;
    }
    
    private HttpResponse<JsonNode> put(String url, Map postData) throws UnirestException, TestRailApiClientException, IOException{
        HttpResponse<JsonNode> response = Unirest.put(configuration.get("jira_url") + url)
                .header("Content-Type", "application/json")
                .basicAuth(configuration.get("username"), configuration.get("password"))
                .body(new JSONObject(postData))
                .asJson();
        if(response.getStatus() != 200){
            throw new TestRailApiClientException("Incorrect response code: " 
                    + Integer.toString(response.getStatus()) + ", message: " 
                    + response.getStatusText() + ", body: " + isToString(response.getRawBody()));
        }
        return response;
    }
    
    private HttpResponse<JsonNode> put(String url) throws UnirestException, TestRailApiClientException, IOException{
        HttpResponse<JsonNode> response = Unirest.put(configuration.get("jira_url") + url)
                .header("Content-Type", "application/json")
                .basicAuth(configuration.get("username"), configuration.get("password"))              
                .asJson();
        if(response.getStatus() != 200){
            throw new TestRailApiClientException("Incorrect response code: " 
                    + Integer.toString(response.getStatus()) + ", message: " 
                    + response.getStatusText() + ", body: " + isToString(response.getRawBody()));
        }
        return response;
    }    
    
    private String isToString(InputStream is) throws IOException{
        int ch;
        StringBuilder sb = new StringBuilder();
        while((ch = is.read()) != -1)
            sb.append((char)ch);
        return sb.toString();
    }
    
    
    /** Test Client API **/
    /**
     * 
     * @param projectId
     * @param runId
     * @return
     * @throws TestRailApiClientException
     * @throws UnirestException
     * @throws IOException 
     */
    private Map getTests(Integer projectId, Integer runId) throws TestRailApiClientException, UnirestException, IOException{
        Map features = new HashMap();
        features.put("projectId", projectId);
        HttpResponse<JsonNode> testRun = get("/index.php?/api/v2/get_run/"+Integer.toString(runId));
        Integer suiteId = testRun.getBody().getObject().getInt("suite_id");
        features.put("suiteId", suiteId);
        
        //** get run configurations **/
        String description = testRun.getBody().getObject().getString("description");
        String[] configRows = description.split("\n");
        Map<String,String> runConfig = new HashMap();
        for(String configRow:configRows){
            String key=configRow.split("=")[0].replaceFirst("!", "");
            String value=configRow.split("=")[1];
            runConfig.put(key, value);
        }
        features.put("runConfig", runConfig);
        
        HttpResponse<JsonNode> testSuite = get("/index.php?/api/v2/get_suite/"+Integer.toString(suiteId));
        String suiteName = testSuite.getBody().getObject().getString("name");
        features.put("suiteName", suiteName);
        
        List featureList = new ArrayList();
        /** get sections **/
        HttpResponse<JsonNode> sections = get("/index.php?/api/v2/get_sections/"
                + projectId + "&suite_id=" + suiteId);        
        /** for each section get test cases in sections **/
        for(int sectionindex=0; sectionindex<sections.getBody().getArray().length(); sectionindex++){
            Map feature = new HashMap();
            JSONObject section = sections.getBody().getArray().getJSONObject(sectionindex);
            feature.put("sectionId", section.getInt("id"));
            feature.put("sectionName", section.getString("name"));
            
            List scenarios = new ArrayList();
            HttpResponse<JsonNode> testCases = get("/index.php?/api/v2/get_cases/"
                    + projectId + "&suite_id=" 
                    + suiteId + "&section_id=" + Integer.toString(section.getInt("id")));
            /** for each test case get scenario**/
            for(int testcaseindex=0; testcaseindex<testCases.getBody().getArray().length(); testcaseindex++){
                Map scenario = new HashMap();
                JSONObject testCase = testCases.getBody().getArray().getJSONObject(testcaseindex);
                scenario.put("testCaseId", testCase.getInt("id"));
                scenario.put("title", testCase.getString("title"));
                String type = "Scenario";
                try{
                    if(testCase.getString("custom_preconds").contains("!type")){
                        if(testCase.getString("custom_preconds").contains("!type=outline")){
                            type = "Scenario Outline";
                        }
                    }
                } catch(Exception e){
                }
                scenario.put("type", type);
                scenario.put("steps", testCase.getString("custom_steps"));
                
                scenarios.add(scenario);
            }
            
            feature.put("scenarios", scenarios);
            featureList.add(feature);
        }
        
        features.put("sections", featureList);
        /** get test case scenario **/
        return features;
    }
    
    /**
     * Pull tests from TestRail runId in JIRA and store them as executeable 
     *  feature files. This method also reads test data from google docs and
     *  updates features with test parameterization
     * 
     * @param projectId
     * @param runId
     * @param location
     * @throws TestRailApiClientException
     * @throws UnirestException
     * @throws IOException 
     */
    public void createFeatureFiles(String projectId, String location)
            throws TestRailApiClientException, UnirestException, IOException {
        
        FileUtils.cleanDirectory(new File(location));
        
        String jql = URLEncoder.encode("project="+projectId+" and status="+toTestStatus+" and type=Story");
        JSONArray stories = get("/rest/api/2/search?jql=" + jql)
                .getBody().getObject().getJSONArray("issues");
        for(int storyindex=0; storyindex<stories.length(); storyindex++){
            JSONObject story = stories.getJSONObject(storyindex);
            String storyId = story.getString("id");
            String storyKey = story.getString("key");
            String storySummary = story.getJSONObject("fields").getString("description");
            
            String featureContent = "Feature: " + storySummary + "\n";
            
            JSONArray subTasks = story.getJSONObject("fields").getJSONArray("subtasks");
            for(int subtaskindex=0; subtaskindex<subTasks.length(); subtaskindex++){
                JSONObject subTask = subTasks.getJSONObject(subtaskindex);
                if(subTask.getJSONObject("fields").getJSONObject("issuetype").getString("name").equals("Sub Test Execution")){
                    String executionId = subTask.getString("id");
                    String executionKey = subTask.getString("key");
                    
                    JSONArray tests = get("/rest/raven/1.0/api/testexec/"+executionKey+"/test")
                            .getBody().getArray();
                    
                    for(int testindex=0; testindex<tests.length(); testindex++){
                        JSONObject test = tests.getJSONObject(testindex);
                        String testId = test.get("id").toString();
                        String testKey = test.getString("key");
                        
                        JSONObject testDetails = get("/rest/api/2/issue/" + testKey)
                                .getBody().getObject();
                        
                        String scenarioName = testDetails.getJSONObject("fields").getString("summary");
                        String scenarioType = testDetails.getJSONObject("fields").getJSONObject("customfield_10007").getString("value");
                        String scenarioGWT = testDetails.getJSONObject("fields").getString("customfield_10008");
                        
                        featureContent += "\n  " + scenarioType + ": " + scenarioName + " - @storyKey:"+storyKey+" @testKey:" + testKey + " @executionKey:" + executionKey +"\n";
                        featureContent += "    " + scenarioGWT.replace("\n", "\n    ");
                        
                        if(scenarioType.contains("Outline")){
                            try{
                            GoogleSheet sheet = new GoogleSheet(testDataFileId);
                            String testData = sheet.exampleTable(
                                    storyKey
                                    , testKey);
                            
                            featureContent += "\n\n    Examples:\n" + testData;
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        
                        featureContent += "\n";
                    }
            
                    System.out.println("[JIRA-Test-Runner]: Found story in Test with ACs: " + storyKey);
                    System.out.println("[JIRA-Test-Runner]: Creating feature file for : " + storyKey);
                    File featureFile = new File(location
                            +"/"+storyKey
                            +"_"+executionKey
                            +".feature");
                    featureFile.createNewFile();
                    FileWriter writer = new FileWriter(featureFile);
                    writer.write(featureContent);
                    writer.flush();
                    writer.close();
                    System.out.println("[JIRA-Test-Runner]: Created feature file: " 
                            + location
                            +"/"+storyKey
                            +"_"+executionKey
                            +".feature");
                }
            }
        }
    }
    
    /**
     * 
     * @throws TestRailApiClientException
     * @throws UnirestException
     * @throws IOException 
     */
    public void createFeatureFiles()
            throws TestRailApiClientException, UnirestException, IOException {
        createFeatureFiles(projectId, "features/");
    }
    
    
    /**
     * 
     * @throws TestRailApiClientException
     * @throws UnirestException
     * @throws IOException 
     */
    public void pushResult()
        throws TestRailApiClientException, UnirestException, IOException{
        pushResult(projectId, "target/cucumber-report.json");
    }
    
    /**
     * 
     * @param projectId
     * @param runId
     * @param resultFilePath
     * @throws TestRailApiClientException
     * @throws UnirestException
     * @throws IOException 
     */
    public void pushResult(String projectId, String resultFilePath)
            throws TestRailApiClientException, UnirestException, IOException{
        String resultsContent = new String(Files.readAllBytes(Paths.get(resultFilePath)));
        JSONArray resultsJson = new JSONArray(resultsContent);
        
        /** deserialize result json file and interprete test outcome **/
        for(int index=0; index<resultsJson.length(); index++){
            JSONObject feature = resultsJson.getJSONObject(index);
            
            Map<String, Map> results = new HashMap();
            
            // for each feature
            for(int scenarioIndex=0; scenarioIndex<feature.getJSONArray("elements").length(); scenarioIndex++){
                
                Map result = new HashMap();
                
                JSONObject scenario = feature.getJSONArray("elements").getJSONObject(scenarioIndex);
                String testKey;
                String executionKey;
                String storyKey;
                String resultId = " ";
                
                String gwt = "";                
                List<Integer> stepResults = new ArrayList();
                
                /** extract testId from results (Scenario title)**/                
                String scenarioName = scenario.getString("name");
                testKey = scenarioName.split("@testKey:")[1].split(" ")[0];
                executionKey = scenarioName.split("@executionKey:")[1].split(" ")[0];
                storyKey = scenarioName.split("@storyKey:")[1].split(" ")[0];
                
                resultId = storyKey + ":" + executionKey + ":" + testKey;
                JSONArray steps = scenario.getJSONArray("steps");
                Integer statusId = 0;

                gwt += "*" + scenario.getString("keyword") + "*: " + scenario.getString("name") + " \n";
                
                for(int stepsIndex=0; stepsIndex<steps.length(); stepsIndex++){
                    String stepResult = steps.getJSONObject(stepsIndex).getJSONObject("result").getString("status");
                    String coloredResult = "";
                    if(stepResult.equals("undefined")){
                        coloredResult = "*{color:#707070}" +stepResult.toUpperCase()+ "{color}*";
                    }else if(stepResult.equals("passed")){
                        coloredResult = "*{color:green}" +stepResult.toUpperCase()+ "{color}*";
                    }else if(stepResult.equals("failed")){
                        coloredResult = "*{color:red}" +stepResult.toUpperCase()+ "{color}*";
                    }else{
                        coloredResult = "*" +stepResult.toUpperCase()+ "*";
                    }
                    gwt += "*{color:blue}" + steps.getJSONObject(stepsIndex).getString("keyword").trim() + "{color}* " + steps.getJSONObject(stepsIndex).getString("name");
                    gwt += " - " + coloredResult + " \n";
                    if(stepResult.equals("undefined")){
                        stepResults.add(1);
                        statusId = 1;
                    }else if(stepResult.equals("passed")){
                        stepResults.add(0);
                    }else if(stepResult.equals("failed")){
                        stepResults.add(3);
                        statusId = 3;
                    }
                }
                
                String fileName = feature.getString("uri").replace(".", "-");
                gwt += configuration.get("jenkins_job_url") + "/" 
                        + System.getProperty("buildNumber") 
                        + "/cucumber-html-reports/" 
                        + fileName + ".html\n\n";
                
                if(results.containsKey(resultId)){
                    String oldGwt = ((Map)results.get(resultId)).get("comments").toString();
                    result.put("comments", oldGwt + "\n" + gwt);
                    Integer oldStatusId = (Integer)((Map)results.get(resultId)).get("status_id");
                    result.put("status_id", Math.max(statusId, oldStatusId));
                }else{
                    result.put("comments", gwt);
//                    result.put("step_results", stepResults);
                    result.put("status_id", statusId);
                }
                results.put(resultId, result);
            }//for each feature
            postResultsToJira(results);
        }        
    }
    
    private void postResultsToJira(Map results) 
            throws UnirestException, TestRailApiClientException, IOException{
        
        List<String> storyKeys = new ArrayList();
        
        for(Object resultKey:results.keySet()){
            String storyKey = resultKey.toString().split(":")[0];
            String executionKey = resultKey.toString().split(":")[1];
            String testKey = resultKey.toString().split(":")[2];
            
            String runId = get("/rest/raven/1.0/api/testrun?testExecIssueKey="+executionKey+"&testIssueKey="+testKey)
                    .getBody().getObject().get("id").toString();
            
            String testResult = "PASS";
            if(!((Map)results.get(resultKey)).get("status_id").toString().equals("0")){
                testResult = "FAIL";
            }
            
            System.out.println("[JIRA-Test-Runner]: Posting test results for Story: " + storyKey + " test: " + testKey);
            System.out.println("[JIRA-Test-Runner]: Test Key: "+ testKey + " Test Result: " + testResult);
            put("/rest/raven/1.0/api/testrun/"+runId+"/status?status="+testResult);
            Map<String, String> comment = new HashMap();
            comment.put("body", ((Map)results.get(resultKey)).get("comments").toString());
            post("/rest/api/2/issue/"+storyKey+"/comment", comment);
            
            if(!storyKeys.contains(storyKey)){
                storyKeys.add(storyKey);
            }
        }
        
        // Triage: update status of story ticket on board
        for(String storyKey:storyKeys){
            JSONObject story = get("/rest/api/2/issue/" + storyKey).getBody()
                    .getObject();
            
            JSONArray subTasks = story.getJSONObject("fields").getJSONArray("subtasks");
            Boolean failed = false;
            for(int subtaskindex=0; subtaskindex<subTasks.length(); subtaskindex++){
                if(subTasks.getJSONObject(subtaskindex).getJSONObject("fields").getJSONObject("issuetype").getString("name").equals("Sub Test Execution")){
                    String testExecutionKey = subTasks.getJSONObject(subtaskindex).getString("key");
                    JSONArray testExecution = get("/rest/raven/1.0/api/testexec/"+testExecutionKey+"/test").getBody()
                            .getArray();
                    for(int testindex=0; testindex<testExecution.length(); testindex++){
                        if(!testExecution.getJSONObject(testindex).getString("status").equals("PASS")){
                            failed = true;
                            break;
                        }
                    }
                }
                if(failed){
                    break;
                }
            }
            
            System.out.println("[JIRA-Test-Runner]: Moving story on board: " + storyKey);
            if(failed){
                Map<String, Map<String, String>> body = new HashMap();
                Map<String, String> transition = new HashMap();
                transition.put("id", failureTransitionId);
                body.put("transition", transition);
                
                post("/rest/api/2/issue/"+storyKey+"/transitions?expand=transitions.fields", body);
            }else{
                Map<String, Map<String, String>> body = new HashMap();
                Map<String, String> transition = new HashMap();
                transition.put("id", successTransitionId);
                body.put("transition", transition);
                
                post("/rest/api/2/issue/"+storyKey+"/transitions?expand=transitions.fields", body);
            }
        }
    }
    
}
