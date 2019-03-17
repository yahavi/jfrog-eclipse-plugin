package org.jfrog.eclipse.configuration;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.jfrog.eclipse.ui.Panel;
import org.jfrog.utils.XrayConnectionUtils;
import org.osgi.framework.FrameworkUtil;

import com.jfrog.xray.client.Xray;
import com.jfrog.xray.client.impl.XrayClient;
import com.jfrog.xray.client.services.system.Version;

/**
 * @author yahavi
 */
public class TestConnectionButton extends FieldEditor {

	private static final String USER_AGENT = "jfrog-eclipse-plugin/"
			+ FrameworkUtil.getBundle(XrayGlobalConfiguration.class).getVersion().toString();
	private StringFieldEditor urlEditor, usernameEditor, passwordEditor;
	private Button button;
	private Text connectionResults;
	private Panel panel;

	public TestConnectionButton(StringFieldEditor urlEditor, StringFieldEditor usernameEditor,
			StringFieldEditor passwordEditor, Composite parent) {
		super("", "Test Connection", parent);
		this.urlEditor = urlEditor;
		this.usernameEditor = usernameEditor;
		this.passwordEditor = passwordEditor;
		createPanel(parent);
		createButton(panel);
		createResultsText(panel);
	}

	private void createPanel(Composite parent) {
		panel = new Panel(parent);
		panel.setLayout(new RowLayout(SWT.VERTICAL));
	}

	private void createButton(Composite parent) {
		button = new Button(parent, SWT.PUSH);
		button.setText(getLabelText());
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					connectionResults.setText("Connecting to Xray...");
					Xray xrayClient = XrayClient.create(urlEditor.getStringValue(), usernameEditor.getStringValue(),
							passwordEditor.getStringValue(), USER_AGENT);
					Version xrayVersion = xrayClient.system().version();

					if (!XrayConnectionUtils.isXrayVersionSupported(xrayVersion)) {
						connectionResults.setText(XrayConnectionUtils.Results.unsupported(xrayVersion));
					} else {
						Pair<Boolean, String> testComponentPermissionRes = XrayConnectionUtils
								.testComponentPermission(xrayClient);
						if (!testComponentPermissionRes.getLeft()) {
							throw new IOException(testComponentPermissionRes.getRight());
						}
						connectionResults.setText(XrayConnectionUtils.Results.success(xrayVersion));
						connectionResults.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
					}
				} catch (IOException | IllegalArgumentException e1) {
					connectionResults.setText(XrayConnectionUtils.Results.error(e1));
					connectionResults.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				}
				panel.pack();
			}
		});
	}

	private void createResultsText(Composite parent) {
		connectionResults = new Text(parent, SWT.LEFT_TO_RIGHT);
		connectionResults.setEnabled(false);
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
	}

	@Override
	protected void doLoad() {
	}

	@Override
	protected void doLoadDefault() {
	}

	@Override
	protected void doStore() {
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}
}