package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class SemanticConfigurationTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {
	
	private static final String NAME = "SPD Configuration";
	private static final String ID = "org.palladiosimulator.analyzer.slingshot.spd.semanticconfigurationtab";
	
	private final ModifyListener modifyListener;
	private Composite container;

	public SemanticConfigurationTab() {
		this.modifyListener = modifyEvent -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		};
	}
	
	@Override
	public void createControl(Composite parent) {
		this.container = new Composite(parent, SWT.NONE);
		setControl(this.container);
		container.setLayout(new GridLayout());
		
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public boolean isValid(final ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		
		return true;
	}
}
