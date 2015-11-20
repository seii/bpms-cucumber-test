package customer.evaluation;

import java.util.HashMap;
import java.util.Map;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;
import cucumber.api.java.After;
import cucumber.api.java.Before;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.WorkflowProcessInstance;

import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

import static org.junit.Assert.assertEquals;

import customer.evaluation.Person;
import customer.evaluation.Request;

/*
 * WARNING:
 * 
 * This step file is not intended as a good example of Behavior-Driven Development (BDD),
 * which is the type of testing that Cucumber strives to encourage. Both feature and step
 * files have been converted from JUnit tests, which are more specifically focused on
 * correct code rather than on emulating user behavior.
 * Use as a guide for coding, please...not as a guide for philosophy!
 * 
 */

/**
 * Step files act as "glue" files that translate a Cucumber feature file, which is in human-
 * readable language, and the actual code necessary to run the tests outlined in the feature
 * file. This file is executing tests against an instance of Red Hat JBoss BPMS, and as such
 * it extends the BPMS JUnit test class "JbpmJUnitBaseTestCase".
 * 
 * This step file executes tests against code pulled from the following publicly-available
 * JBoss demo: {@link https://github.com/jbossdemocentral/bpms-customer-evaluation-demo}
 * 
 * @author dlaffran
 * @see org.jbpm.test.JbpmJUnitBaseTestCase
 *
 */
public class StepFile extends JbpmJUnitBaseTestCase {
   private Person person;
   private Request request;
   
   private static Integer underAged    = 11;
   private static Integer adultAged    = 25;
   private static Integer richCustomer = 2000; // greater than 999.
   private static Integer poorCustomer  = 2;
   private static RuntimeEngine runtime;

   @Before
   public void setup() {
	   Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
	   resources.put("customereval.bpmn2", ResourceType.BPMN2);
	   resources.put("financerules.drl", ResourceType.DRL);

	   createRuntimeManager(Strategy.SINGLETON, resources);
	   runtime = getRuntimeEngine(ProcessInstanceIdContext.get());
   }
   
   //Scenario: Evaluating an underage customer
   //Scenario: Evaluating a poor customer
   //Scenario: Evaluating a rich customer
   @Given("^Some customer information$")
   public void some_customer_information() {
	   person = new Person("bob", "Bob Smith");
	   request = new Request("1");
	   request.setPersonId("bob");
   }
   
   //Scenario: Evaluating an underage customer
   @When("^The customer is not of legal age$")
   public void the_customer_is_not_of_legal_age() {
	   person.setAge(underAged);
	   //Either rich or poor can be set, "poor" is picked for this example
	   request.setAmount(poorCustomer);
   }
   
   @Then("^Flag customer as underage$")
   public void flag_customer_as_underage() {
	   // Map to be passed to the startProcess.
	   Map<String, Object> params = new HashMap<String, Object>();
	   params.put("person", person);
	   params.put("request", request);

	   // Fire it up!
	   System.out.println("=========================================");
	   System.out.println("= Starting Process Underaged Test Case. =");
	   System.out.println("=========================================");

	   KieSession ksession = runtime.getKieSession();
	   ksession.insert(person);
	   ProcessInstance pi = ksession.startProcess("customer.evaluation", params);
	   ksession.fireAllRules();

	   // Finished.
	   assertProcessInstanceCompleted(pi.getId(), ksession);
	   assertNodeTriggered(pi.getId(), "Underaged");
	   ksession.dispose();

	   System.out.println("======================================");
	   System.out.println("= Ended Process Underaged Test Case. =");
	   System.out.println("======================================");
   }
   
   //Scenario: Evaluating a poor customer
   @When("^The customer has less than (\\d+) dollars$")
   public void the_customer_has_less_than_dollars(Integer wealth) {
	   person.setAge(adultAged);
	   request.setAmount(poorCustomer);
   }
   
   @Then("^Flag customer as poor$")
   public void flag_customer_as_poor() {
	   KieSession ksession = runtime.getKieSession();
	   //KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newThreadedFileLogger(ksession, "CustomerEvaluationPoorAdult", 1000);
	
	   ksession.insert(person);

	   // Map to be passed to the startProcess.
	   Map<String, Object> params = new HashMap<String, Object>();
	   params.put("person", person);
	   params.put("request", request);
				
	   // Fire it up!
	   System.out.println("==========================================");
	   System.out.println("= Starting Process Poor Adult Test Case. =");
	   System.out.println("==========================================");

	   WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess("customer.evaluation", params);
	   ksession.insert(processInstance);
	   ksession.fireAllRules();
						
	   // Finished, clean up the logger.
	   assertProcessInstanceCompleted(processInstance.getId(), ksession);
	   assertNodeTriggered(processInstance.getId(), "End Poor Customer");
	   //logger.close();
	   ksession.dispose();
   }
   
   //Scenario: Evaluating a rich customer
   @When("^The customer has more than (\\d+) dollars$")
   public void the_customer_has_more_than_dollars(Integer wealth) {
	   person.setAge(adultAged);
	   request.setAmount(richCustomer);
   }
   
   @Then("^Flag customer as rich$")
   public void flag_customer_as_rich() {
	   KieSession ksession = runtime.getKieSession();
	   
	   ksession.insert(person);

	   // Map to be passed to the startProcess.
	   Map<String, Object> params = new HashMap<String, Object>();
	   params.put("person", person);
	   params.put("request", request);
		
	   // Fire it up!
	   System.out.println("==========================================");
	   System.out.println("= Starting Process Rich Adult Test Case. =");
	   System.out.println("==========================================");

	   WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess("customer.evaluation", params);
	   ksession.insert(processInstance);
	   ksession.fireAllRules();
				
	   // Finished, clean up the logger.
	   assertProcessInstanceCompleted(processInstance.getId(), ksession);
	   assertNodeTriggered(processInstance.getId(), "End Rich Customer");
	   //logger.close();
	   ksession.dispose();
   }
   
   @After
   public void teardown() {
	   disposeRuntimeManager();
   }
}
