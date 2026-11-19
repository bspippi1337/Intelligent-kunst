package no.blckswan.intelligentkunst.render;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ImageStructureTest {
    @Test public void constantReferenceHasNoFalseContours(){
        int[] pixels=new int[32*20];
        java.util.Arrays.fill(pixels,0xFFA8B4C0);
        ImageStructure field=new ImageStructure(pixels,32,20);
        for(int y=0;y<20;y++)for(int x=0;x<32;x++){
            assertEquals(0,field.edge(x,y),0.00001f);
            assertEquals(0,field.contourAngle(x,y),0.00001f);
        }
    }

    @Test public void verticalContrastProducesVerticalBrushTangents(){
        int w=64,h=48;int[] pixels=new int[w*h];
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            pixels[y*w+x]=x<32?0xFF000000:0xFFFFFFFF;
        }
        ImageStructure field=new ImageStructure(pixels,w,h);
        assertTrue(field.edge(31,24)>.7f);
        assertEquals(0f,Math.abs((float)Math.cos(field.contourAngle(31,24))),.01f);
        assertTrue(field.edge(5,24)<.001f);
        assertEquals(0f,field.luminance(5,24),.001f);
        assertEquals(1f,field.luminance(56,24),.001f);
    }

    @Test public void horizontalContrastProducesHorizontalBrushTangents(){
        int w=48,h=64;int[] pixels=new int[w*h];
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            pixels[y*w+x]=y<32?0xFFFFFFFF:0xFF000000;
        }
        ImageStructure field=new ImageStructure(pixels,w,h);
        assertTrue(field.edge(24,31)>.7f);
        assertEquals(0f,Math.abs((float)Math.sin(field.contourAngle(24,31))),.01f);
    }

    @Test public void validatesMemoryBoundsAndSourceIntegrity(){
        try{new ImageStructure(new int[3],2,2);fail("accepted malformed buffer");}
        catch(IllegalArgumentException expected){}
        try{new ImageStructure(new int[513*3],513,3);fail("accepted huge sample");}
        catch(IllegalArgumentException expected){}
        int[] source=new int[25];
        java.util.Arrays.fill(source,0xFF556677);
        ImageStructure field=new ImageStructure(source,5,5);
        source[12]=0;
        assertEquals(0xFF556677,field.color(2,2));
        // Border samples are safe under overrun/crop conditions.
        assertEquals(0xFF556677,field.color(-100,1000));
        assertEquals(field.edge(0,0),field.edge(-100,-100),0.001f);
    }
}
