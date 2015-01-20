package gov.epa.nrmrl.std.lca.ht.harmonizationtool;

import gov.epa.nrmrl.std.lca.ht.perspectives.LCIWorkflowPerspective;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		System.out.println("configurer.setSaveAndRestore(true)");
		configurer.setSaveAndRestore(true);
		super.initialize(configurer);
	}

	public String getInitialWindowPerspectiveId() {
//		return FlowDataV1.ID;
//		return FlowDataV4.ID;
		return LCIWorkflowPerspective.ID;
//		return OriginalPerspective.ID;
	}

}