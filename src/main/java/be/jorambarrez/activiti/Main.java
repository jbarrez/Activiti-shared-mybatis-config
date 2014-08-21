package be.jorambarrez.activiti;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Joram Barrez
 */
public class Main {
	
	private static List<ProcessEngine> regularProcessEngines = new ArrayList<ProcessEngine>();
	
	private static List<ProcessEngine> sharedCfgProcessEngines = new ArrayList<ProcessEngine>();
	
	public static void main(String[] args) throws Exception {
		
		Scanner scanner = new Scanner(System.in);
		
	  String choice = null;
	  while (true) {
	  	
	  	showOptions();
	  	choice = scanner.nextLine();
	  	
	  	if ("1".equals(choice)) {
	  		System.out.println();
	  		System.out.println("How many regular process engines do you want to boot?");
	  		System.out.println();
	  		int nr = scanner.nextInt();
	  		bootRegularProcessEngines(nr);
	  	} else if ("2".equals(choice)) {
	  		System.out.println();
	  		System.out.println("How many process engines with shared mybatis cfg do you want to boot?");
	  		System.out.println();
	  		int nr = scanner.nextInt();
	  		bootSharedMybatisCfgProcessEngine(nr);
	  	} else if ("3".equals(choice)) {
	  		testProcessEngines();
	  	}
	  	
	  	
	  }
	  
  }

	private static void showOptions() {
		System.out.println();
		System.out.println("---------------------------------------------------------------------");
	  System.out.println("Enter the number to select one of the following choices:");
	  System.out.println();
	  System.out.println("1) Boot a regular Activiti Process Engine");
	  System.out.println("2) Boot an Activiti Process Engine with shared Mybatis configuration");
	  System.out.println("3) Test all process engines by running test process instance");
	  System.out.println("---------------------------------------------------------------------");
	  System.out.println();
  }
	
	private static void bootRegularProcessEngines(int number) {
		for (int i=0; i<number; i++) {
			System.out.println("Booting engine" + (i+1));
			
			ProcessEngine processEngine = ProcessEngineConfiguration
					.createProcessEngineConfigurationFromResource("activiti.cfg.xml")
					.buildProcessEngine();
			regularProcessEngines.add(processEngine);
		}
		
		System.out.println();
		System.out.println("Number of regular process engines: " + regularProcessEngines.size());
		System.out.println();
	}
	
	private static void bootSharedMybatisCfgProcessEngine(int number) {
		for (int i=0; i<number; i++) {
			System.out.println("Booting engine" + (i+1));
			ProcessEngine processEngine = ProcessEngineConfiguration
					.createProcessEngineConfigurationFromResource("activiti-shared-mybatis-cfg.cfg.xml")
					.buildProcessEngine();
			sharedCfgProcessEngines.add(processEngine);
		}
		
		System.out.println();
		System.out.println("Number of process engines with shared Mybatis config: " + sharedCfgProcessEngines.size());
		System.out.println();
	}
	
	private static void testProcessEngines() {
		int count = 0;
		
		List<ProcessEngine> allEngines = new ArrayList<ProcessEngine>();
		allEngines.addAll(regularProcessEngines);
		allEngines.addAll(sharedCfgProcessEngines);
		
		for (ProcessEngine processEngine : allEngines) {
				System.out.println("Testing process engine instance " + processEngine);
				processEngine.getRepositoryService().createDeployment().addBpmnModel("oneTask.bpmn20.xml", createOneTaskBpmnModel()).deploy();
				ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");
				if (processInstance == null) {
					throw new RuntimeException("Process engine failure: null process instance");
				}
				System.out.println((count++) + " process engines tested");
		}
	}
		
	
	private static BpmnModel createOneTaskBpmnModel() {
		BpmnModel model = new BpmnModel();
		model.addProcess(createOneTaskProcess());
		return model;
	}
	
	private static org.activiti.bpmn.model.Process createOneTaskProcess() {
		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();

		process.setId("oneTaskProcess");
		process.setName("The one task process");

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");
		process.addFlowElement(startEvent);

		UserTask userTask = new UserTask();
		userTask.setName("The Task");
		userTask.setId("theTask");
		userTask.setAssignee("kermit");
		process.addFlowElement(userTask);

		EndEvent endEvent = new EndEvent();
		endEvent.setId("theEnd");
		process.addFlowElement(endEvent);

		process.addFlowElement(new SequenceFlow("start", "theTask"));
		process.addFlowElement(new SequenceFlow("theTask", "theEnd"));

		return process;
	}

}
