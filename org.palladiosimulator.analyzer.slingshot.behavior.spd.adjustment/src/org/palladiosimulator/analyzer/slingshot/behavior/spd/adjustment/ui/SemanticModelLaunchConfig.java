package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.ui;

import org.palladiosimulator.analyzer.slingshot.core.extension.SystemBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.analyzer.slingshot.ui.events.ArchitectureModelsTabBuilderStarted;
import org.palladiosimulator.analyzer.slingshot.ui.events.CustomTabBuilderInitialized;
import org.palladiosimulator.analyzer.slingshot.workflow.events.WorkflowLaunchConfigurationBuilderInitialized;
import org.palladiosimulator.monitorrepository.MonitorRepository;
import org.palladiosimulator.semanticspd.Configuration;

@OnEvent(when = ArchitectureModelsTabBuilderStarted.class)
@OnEvent(when = WorkflowLaunchConfigurationBuilderInitialized.class)
@OnEvent(when = CustomTabBuilderInitialized.class)
public class SemanticModelLaunchConfig implements SystemBehaviorExtension {

	private static final String FILE_NAME = "semanticspd";
	
	@Subscribe
	public void onArchitectureModelsTab(final ArchitectureModelsTabBuilderStarted tab) {
		tab.newModelDefinition()
			 .fileName(FILE_NAME)
			 .modelClass(Configuration.class)
			 .label("SPD Semantic Configuration")
			 .optional(true)
			 .build();
	}
	
	@Subscribe
	public void onWorkflowConfiguration(final WorkflowLaunchConfigurationBuilderInitialized init) {
		init.getConfiguration(FILE_NAME, 
				"semanticspd", 
				(conf, model) -> conf.addOtherModelFile((String) model));
	}
	
	
	@Subscribe
	public void onCustomTabBuilderInitialized(final CustomTabBuilderInitialized tabBuilder) {
		tabBuilder.getTabList().addTab(new SemanticConfigurationTab());
	}
}
