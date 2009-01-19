/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package minimalsurface.frontend.action;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;

/**
 * @author schmies
 * 
 */
public class WriterOBJ {

	public static int write( IndexedFaceSet ifs, OutputStream out, int startVertex ) {
		return write( ifs, "", new PrintWriter( out ), startVertex);
	}
	
	public static int write( IndexedFaceSet ifs, OutputStream out) {
		return write( ifs, "", new PrintWriter( out ), 0);
	}
	
	static void write( PrintWriter out, double [] array, String seperator ) {
		if( array==null || array.length==0) return;
		out.print(array[0]);
		for( int i=1; i<array.length; i++ ) {
			out.print(seperator);
			out.print(array[i]);
		}
	}
	
	static void write( PrintWriter out, double [][] array, String prefix ) {
		if( array==null) return;
		String seperator = " ";
		for( int i=0; i<array.length; i++ ) {
			out.print(prefix);
			out.print( seperator );
			write(out, array[i], seperator );
			out.println();
		}
	}

	static int write( Geometry geom, String groupName, PrintWriter out, int startVertex ) {
		if( geom == null ) return 0;
		
		if( geom instanceof IndexedFaceSet ) {
			return write( ((IndexedFaceSet)geom), groupName, out, startVertex);
		} else {
			 LoggingSystem.getLogger(GeometryUtility.class).log(Level.WARNING, 
					 	"ignoring scene graph component " + groupName );
		}
		return 0;
	}
	

	public static void write( SceneGraphComponent sgc, OutputStream out) {
		write( sgc, new PrintWriter( out ));
	}
	
	public static void write( SceneGraphComponent sgc, PrintWriter out ) {
		
		SceneGraphComponent flat = flatten(sgc);
		
		int vertex = write( flat.getGeometry(), flat.getName(), out, 0);
		
		final int noc = flat.getChildComponentCount();
			
		for( int i=0; i<noc; i++ ) {
			SceneGraphComponent child=flat.getChildComponent(i);
			vertex += write( child.getGeometry(), child.getName(), out, vertex);
		}
	}
	
	static void writeFaceIndex( PrintWriter out, int index, boolean hasTexture, boolean hasNormals ) {
		out.print(index+1);
		if( !hasTexture && !hasNormals ) return;
		out.print("/");
		if( hasTexture ) out.print(index+1);
		if( !hasNormals ) return;
		out.print("/");
		out.print(index+1);
	}

	static int write( IndexedFaceSet ifs, String groupName, PrintWriter out, int startVertex ) {
		groupName = groupName.replace(' ', '_');
		if( groupName != null ) {
			out.println();	
			out.println( "g " + groupName );
		    out.println();
		}

		double [][] points = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().toDoubleArrayArray(null);
		if (points[0].length == 4)	{
			// dehomogenize!
			double[][] points3 = new double[points.length][3];
			Pn.dehomogenize(points3, points);
			points = points3;
		}
        final double [][] normals;
		if( ifs.getVertexAttributes( Attribute.NORMALS ) != null ) {
			normals = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray().toDoubleArrayArray(null);
		} else {
			normals = null;
		}
		final double [][] texture;
		if( ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES ) != null ) {
			texture = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray().toDoubleArrayArray(null);
		} else {
			texture = null;
		}
		
		write( out, points, "v" );
		write( out, texture, "vt" );
		write( out, normals, "vn" );

		out.println();

		DataList indices = ifs.getFaceAttributes(Attribute.INDICES  );
		
		for (int i= 0; i < ifs.getNumFaces(); i++) {
			out.print( "f  ");
			IntArray faceIndices=indices.item(i).toIntArray();
			writeFaceIndex( out, startVertex + faceIndices.getValueAt(0), texture!=null, normals!=null );	
			for (int j= 1; j < faceIndices.size(); j++) {
				out.print( " " );
				writeFaceIndex( out, startVertex + faceIndices.getValueAt(j), texture!=null, normals!=null );
			}

			out.println();	
		}

		out.flush();
		return ifs.getNumPoints();
	}
	
	
	 public static SceneGraphComponent flatten(SceneGraphComponent sgc)		{
			
	    final double[] flipit = P3.makeStretchMatrix(null, new double[] {-1,0, -1,0, -1.0});
		final ArrayList<SceneGraphComponent> geoms = new ArrayList<SceneGraphComponent>();
	    SceneGraphVisitor v =new SceneGraphVisitor() {
	    	    SceneGraphPath thePath = new SceneGraphPath();
	    	    
           @Override
		public void visit(PointSet oldi) {
           	// have to copy the geometry in case it is reused!
           	PointSet i = SceneGraphUtility.copy(oldi);
           	//System.err.println("point set is "+i);
           	if (i.getVertexAttributes(Attribute.COORDINATES) == null) return;
          	    double[][] v = i.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
           	double[] currentMatrix = thePath.getMatrix(null);
           	double[][] nv = Rn.matrixTimesVector(null, currentMatrix, v);
           	i.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
               double[] cmp = null;
        	    if (i instanceof IndexedFaceSet)	{
           	    IndexedFaceSet ifs = (IndexedFaceSet) i; //(IndexedFaceSet) SceneGraphUtility.copy(i); //
                   double[] mat = Rn.transpose(null, currentMatrix);          	
                   mat[12] = mat[13] = mat[14] = 0.0;
                   Rn.inverse(mat, mat);
//	             	   if (Rn.determinant(currentMatrix) < 0.0)	cmp = Rn.times(null, flipit, mat);
//	             	   else 
            	   cmp = mat;
           	   if (ifs.getFaceAttributes(Attribute.NORMALS) != null)	{
              	   //System.out.println("Setting face normals");
           	v = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
                   nv = Rn.matrixTimesVector(null, cmp, v);
                   ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
           	       } else IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs);
              	   if (ifs.getVertexAttributes(Attribute.NORMALS) != null)	{
          	   		//System.out.println("Setting vertex normals");
                     v = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
                       nv = Rn.matrixTimesVector(null, cmp, v);
                       ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
           	       } else IndexedFaceSetUtility.calculateAndSetVertexNormals(ifs);
             	   if (Rn.determinant(currentMatrix) < 0.0)	{           	
              	   		//System.out.println("Flipping normals");
              	   		v = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
              	   		nv = Rn.matrixTimesVector(null, flipit, v);
              	   		ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
              	   		v = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
              	   		nv = Rn.matrixTimesVector(null, flipit, v);
              	   		ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
            	   }
          	   }
        	   //System.out.println("det is "+Rn.determinant(currentMatrix));
//		          if (Rn.determinant(currentMatrix) < 0.0)	{
	                SceneGraphComponent foo = new SceneGraphComponent();
	                foo.setGeometry(i);
	                if (thePath.getLastComponent().getAppearance() != null)	{
	                	foo.setAppearance(thePath.getLastComponent().getAppearance());
	                }
	                geoms.add(foo);
//	          	   }
            }
           @Override
		public void visit(SceneGraphComponent c) {
           	if (!c.isVisible())
           		return;
           	thePath.push(c);
               c.childrenAccept(this);
              //if (c.getTransformation() != null) c.getTransformation().setMatrix(Rn.identityMatrix(4));
              //c.setName(c.getName() + "_flat");
               thePath.pop();
           }
           @Override
		public void visit(Sphere s)	{
           	    LoggingSystem.getLogger(GeometryUtility.class).log(Level.WARNING, "Can't flatten a sphere yet");
           }
       };
       v.visit(sgc);
       SceneGraphComponent flat = new SceneGraphComponent();
       if (sgc.getAppearance() != null) flat.setAppearance(sgc.getAppearance());
       for (Iterator<SceneGraphComponent> iter = geoms.iterator(); iter.hasNext();) {
            SceneGraphComponent foo = iter.next(); ;
            flat.addChild(foo);
      }
       //GeometryUtility.calculateFaceNormals(flat);
       //GeometryUtility.calculateVertexNormals(flat);
      return flat;
	}
	
	
}
