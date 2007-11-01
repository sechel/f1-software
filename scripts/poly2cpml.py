#!/usr/bin/python
#
# poly2cpml by kristoffer josefsson
# version 0.3, (c) 2006
# josefsso@math.tu-berlin.de
# TU-Berlin, Germany
#
# takes a triangulated 3-polytope in polymakeformat
# and converts it to xml for the alexandrov polytope application
# does not check anything
# please make sure VERTICES, VERTICES_IN_FACETS is in the input file
# (this can be done by running 'polymake infile.poly VERTICES_IN_FACETS' for example)
#
# TODO: clean up code?

import sys
from math import sqrt

from types import TupleType, ListType
def flatten( seq ):
	res = []
	for item in seq:
		if type( item ) in ( TupleType, ListType ):
			res.extend( flatten( item ) )
		else:
			res.append( item )
	return res
	
class GeometryReader:

	def getSegment( self, data, word1, word2 ):
		i = data.index( word1 )+1
		temp = data[i:]
		j = temp.index( word2 )
		temp = temp[0:j]
		return temp
		
	def facets2edges( self, facets, vertices ):
		a = {}
		for f in facets:
			f.append( f[0] ) 
			for i in range( len( f )-1 ):
				a[( f[i], f[i+1] )] = self.edgeLength( vertices[f[i]], vertices[f[i+1]] )
		return a
		
	def cleanEdges( self, edgedict ):
		d = {}
		for k, v in edgedict.iteritems():
			a, b = k
			if not d.has_key( ( b, a ) ):
				d[k]=v  
		return d	

	def facet2edges( self, f, vertices ):
		a = [( f[i], f[i+1] ) for i in range( len( f )-1 )]
		return a		
		
	def edgeLength( self, v1, v2 ):
		return sqrt( ( v1[0]-v2[0] )**2 + ( v1[1]-v2[1] )**2 + ( v1[2]-v2[2] )**2 )
		

class POLYReader(GeometryReader):

	def __init__(self):
		return

	def str2vertex(self, string):
		temp = string[2:]
		t1 = [map(int , a.split('/')) for a in temp.split(' ')]
		t2 = []
		for a in t1:
			if len(a) == 1:
				t2.append([a[0], 1])
			else:
				t2.append([a[0],a[1]])
		t2 = [a[0] / a[1] for a in t2]
		return t2

	def facet2vertexlist( self, string ):
		return map(int, self.getSegment(string, "{", "}").split( ' ' ))
		
	def load( self, filename ):
		f = open( filename, 'r' )
		data = f.readlines() 
		vertices = map( self.str2vertex, self.getSegment( data, "VERTICES\n" , "\n") )
		facets = map( self.facet2vertexlist, self.getSegment( data, "VIF_CYCLIC_NORMAL\n", "\n" ) )
		self.comb = self.facets2edges( facets, vertices )
		self.comb = self.cleanEdges( self.comb )
		self.fcomb = [self.facet2edges( f, vertices ) for f in facets]

class JVXReader(GeometryReader):

	def __init__(self):
		return

	def load(self, filename):
		f = open(filename, 'r')
		data = f.readlines()
		
		vertices = map(self.str2vector, self.getSegment(data, "<points>\n", "</points>\n"))
		facets = map(self.facet2vertexlist, self.getSegment(data, "<faces>\n", "</faces>\n"))
		
		self.comb = self.facets2edges( facets, vertices )
		self.comb = self.cleanEdges( self.comb )
		
		self.fcomb = [self.facet2edges( f, vertices ) for f in facets]

	def facet2vertexlist( self, string ):
		return map(int, self.getSegment(string, "<f>", "</f>").split(' '))
		
	def str2vector(self, string):
		temp = self.getSegment(string, "<p>", "</p>")
		t = [x for x in temp.split(" ")]
		return t
		

class OBJReader(GeometryReader):

	def __init__(self):
		return

	def load(self, filename):
		f = open(filename, 'r')
		data = f.readlines()
	
		vertices = []
		facets = []

		for line in data:
			if line[0:2] == "v ":
				vertices.append( self.str2vector(line) )
			if line[0:2] == "f ":
				facets.append( map(self.facet2vertexlist, line.split(' ')[2:] ))


		self.comb = self.facets2edges( facets, vertices )
		self.comb = self.cleanEdges( self.comb )
		
		self.fcomb = [self.facet2edges( f, vertices ) for f in facets]

	def str2vector(self, string):
		temp = self.getSegment(string, "v", "\n")[1:]
		t = [float(x) for x in temp.split(" ")]
		return t

	def facet2vertexlist(self, string):
		return int(string.split('//')[0])-1

class CPMLWriter:

	header = ["<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n", 
			  "<!DOCTYPE convexPolyhedralMetric>\n", 
			  "<cpml description=\"converted from polymake with poly2cpml\">\n"]
			  
	footer = ["</cpml>"]

	def __init__( self, infile, outfile ):
	
		if infile[-3:] == "jvx":
			self.p = JVXReader()
		if infile[-4:] == "poly":
			self.p = POLYReader()
		if infile[-3:] == "obj":
			self.p = OBJReader()
			
		self.p.load(infile)
	#	self.load( infile )
		self.write( outfile )
	
	def write( self, filename ):
		f = open( filename, 'w' )
		
		for line in self.header:
			f.write( line )
			
		f.write( "\t<edgelist>\n" )
	
		for k, v in self.p.comb.iteritems():
			f.write( "\t\t<edge length=\"" + str( v ) + "\"/>\n" )
		f.write( "\t</edgelist>\n" )
		
		f.write( "\t<trianglelist>\n" )
		for face in self.p.fcomb:
			tri = []
			for key in face:
				a, b = key
				keys = self.p.comb.keys()
				if ( a, b ) in keys:		
					tri.append( keys.index( ( a, b ) ) )
				else:
					tri.append( keys.index( ( b, a ) ) )
			a, b, c = tri
			s = "\t\t<triangle a=\"%s\" b=\"%s\" c=\"%s\"/>\n" % ( a, b, c )
			f.write( s )
		f.write( "\t</trianglelist>\n" )
		
		for line in self.footer:
			f.write( line )
		
if __name__ == "__main__":
	if len(sys.argv) < 3:
		s = "Usage: %s infile outfile" % (sys.argv[0])
		print(s)
	else:
		c = CPMLWriter( sys.argv[1], sys.argv[2] )
