package alexandrov.frontend.controller;

import alexandrov.frontend.content.CPMLEditor;

public interface MainProgramCapabilities {

	public void switchToXMLMode();
	
	public void switchToGraphMode();
	
	public CPMLEditor getCpmlEditPanel();
	
}
