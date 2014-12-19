package osw;

import java.util.HashMap;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.SimpleStep;

public class GenericTestMainOne {

	public static void main(String[] args) {

		ClassPathXmlApplicationContext beanFactory = new ClassPathXmlApplicationContext(
				new String[] { "WEB-INF/views-servlet.xml" });

		Workflow wf = (Workflow) beanFactory.getBean("workflow");

		long id = 89;

		try {
			id = wf.initialize("c4cfd35c-78ac-4655-9274-c59f0927ab88", 500, new HashMap<String, String>());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		int[] actions = wf.getAvailableActions(id, null);
		WorkflowDescriptor wd = wf.getWorkflowDescriptor(wf.getWorkflowName(id));

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

		beanFactory.close();
	}
}
