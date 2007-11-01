
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


package de.jreality.geometry;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;

/**
* Static methods for constructing, extracting, and modifying 
* instances of {@link IndexedLineSet}.
* @author Charles Gunn
* {@see IndexedLineSetUtility} for more ways to specify instances of {@link IndexedLineSet}.
*
*/
public class IndexedLineSetUtility2 {


	
	/**
	 * @author Kristoffer Josefsson
	 * @param points
	 * @param closed
	 * @return
	 */
	public static IndexedLineSet createPlaneCurveFromCruvature(IndexedLineSet g, final double[] curvature, final double[] lengths, final double[] base, final double[] baseTangent, final double[] normal, boolean closed)	{

		int n = curvature.length;
		int m = lengths.length;
		
		int size = Math.min(n, m)+1;
		
		double[][] points = new double[size][3];
		
		points[0][0] = base[0];
		points[0][1] = base[1];
		points[0][2] = base[2];
		
		double[] dir = new double[3];
		dir[0] = baseTangent[0];
		dir[1] = baseTangent[1];
		dir[2] = baseTangent[2];
		
		Rn.normalize(normal, normal);
		
//		curvature[0] = 0.0;
		
		for(int i = 1; i < size; i++) {

			Rn.normalize(dir, dir);
			
			Matrix rot = MatrixBuilder.euclidean().rotate(curvature[i-1], normal[0],normal[1],normal[2]).getMatrix();
			double[] t = new double[4];
			t = rot.multiplyVector(new double[]{dir[0], dir[1], dir[2], 1.0});
			
			dir[0] = t[0];
			dir[1] = t[1];
			dir[2] = t[2];
			
			points[i][0] = points[i-1][0] + lengths[i-1]*dir[0];
			points[i][1] = points[i-1][1] + lengths[i-1]*dir[1]; 
			points[i][2] = points[i-1][2] + lengths[i-1]*dir[2];
			
			dir[0] = points[i][0] - points[i-1][0];
			dir[1] = points[i][1] - points[i-1][1];
			dir[2] = points[i][2] - points[i-1][2];
			
		}
		
		return IndexedLineSetUtility.createCurveFromPoints(points, closed);
	}

}
