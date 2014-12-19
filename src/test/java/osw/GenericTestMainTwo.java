package osw;

import java.util.Collections;
import java.util.Scanner;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.SimpleStep;

public class GenericTestMainTwo {

	public static void main(String[] args) throws InvalidInputException, WorkflowException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		ClassPathXmlApplicationContext beanFactory = new ClassPathXmlApplicationContext(
				new String[] { "WEB-INF/views-servlet.xml" });

		Workflow wf = (Workflow) beanFactory.getBean("workflow");

		long id = 308;

		// com.opensymphony.workflow.util.beanshell.BeanShellFunctionProvider

		id = wf.initialize("9fc06c40-021b-4483-9b0a-6587e0cfce2e", 100,
				Collections.EMPTY_MAP);

		if (1 == 1) {
			return;
		}

		int[] actions = wf.getAvailableActions(id, null);
		WorkflowDescriptor wd = wf.getWorkflowDescriptor(wf.getWorkflowName(id));

		try {
			wf.doAction(id, 601, Collections.EMPTY_MAP);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (1 == 1) {
			return;
		}

		if (actions.length > 0) {

			System.out.println("Action: " + actions[0]);

			if (1 == 1) {
				return;
			}

			try {
				wf.doAction(id, actions[0], Collections.EMPTY_MAP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println(id);

		SimpleStep s = (SimpleStep) wf.getCurrentSteps(id).get(0);

		System.out.println(s.getOwner());

		// wf.getPropertySet(id).setString("stringkey", "value1");

		System.out.println("------------------------------");
		for (int i = 0; i < actions.length; i++) {
			ActionDescriptor a = wd.getAction(actions[i]);
			System.out.println(a.getName());
			System.out.println(a.getMetaAttributes());
		}

		System.out.println("------------------------------");

		System.out.println(wf.getEntryState(id));

		Scanner scan = new Scanner(System.in);
		scan.next();

		beanFactory.close();
	}
}
