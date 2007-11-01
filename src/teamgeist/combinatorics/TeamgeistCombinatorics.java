package teamgeist.combinatorics;

import halfedge.HalfEdgeDataStructure;
import halfedge.io.HESerializableReader;

import java.io.IOException;
import java.io.InputStream;

import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class TeamgeistCombinatorics {

	
	@SuppressWarnings("unchecked")
	public static HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> getTeamgeistGraph(){
		InputStream in = TeamgeistCombinatorics.class.getResourceAsStream("teamgeist_triang2_cut.cpm");
		HESerializableReader<CPMVertex, CPMEdge, CPMFace> reader = null;
		try {
			reader = new HESerializableReader<CPMVertex, CPMEdge, CPMFace>(in);
			return reader.readHalfEdgeDataStructure();
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
