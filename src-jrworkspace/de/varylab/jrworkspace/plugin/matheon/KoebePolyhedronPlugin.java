package de.varylab.jrworkspace.plugin.matheon;

import java.awt.Container;

import koebe.frontend.KoebesPolyhedron;
import util.debug.DBGTracer;
import de.jreality.ui.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.jrdesktop.JRDesktopFrame;

public class KoebePolyhedronPlugin extends JRDesktopFrame {

	@Override
	protected Container getContent() {
		DBGTracer.setActive(false);
		KoebesPolyhedron mainApp = new KoebesPolyhedron();
		mainApp.validate();
		Container c = mainApp.getContentPane();
		c.setSize(800, 500);
		return c;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Koebe Polyhedron";
		info.vendorName = "Stefan Sechelmann";
		info.version = 0x01000000;
		info.icon = null;
		return info;
	}

}
