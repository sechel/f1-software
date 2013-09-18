package util;

import halfedge.HalfEdgeDataStructure;
import halfedge.io.HESerializableReader;
import halfedge.util.Consistency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class TestData {

	public static HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> getTestGraph(String filename){
		File testFile = new File("src-testing/data/" + filename);
		try {
			FileInputStream fis = new FileInputStream(testFile);
			HESerializableReader<CPMVertex, CPMEdge, CPMFace> reader = new HESerializableReader<CPMVertex, CPMEdge, CPMFace>(fis);
			HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> result = reader.readHalfEdgeDataStructure();
			if (!Consistency.checkConsistency(result))
				System.err.println("!! Incorrect Data in getTestGraph() !!");
			reader.close();
			return result;
		} catch (FileNotFoundException fnfe){
			fnfe.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe){
			cnfe.printStackTrace();
		}
		return null;
	}

}
