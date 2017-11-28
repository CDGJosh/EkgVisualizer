package ekg.visualizer;

/**
 * Created by Josh on 05.11.2017.
 */

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLU;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.GLU.*;

public class GLCube {

    private int shader;
    public int getShader() {
        return shader;
    }


    private final String cubeVSSrc =
                    "attribute vec3 vertex;" +
                    "uniform vec3 position;"+
                    "uniform vec3 center;"+
                    "uniform vec3 rotation;"+
                    "uniform float scale;"+
                    "uniform float zoom;"+
                    "uniform float zClamp;"+
                    "uniform mat4 rot;"+
                    "void main() {" +
                            "float yang = 0.0;"+//785;"+
                            "float xang = 0.0;"+
                            "mat4 trans = mat4(1.0,     0.0, 0.0, 0.0,    0.0, 1.0,   0.0, 0.0,    0.0, 0.0, 1.0,   0.0,    position.x - center.x, position.y - center.y, position.z - center.z, 1.0);"+
                            "mat4 scaling = mat4(scale, 0.0, 0.0, 0.0,    0.0, scale, 0.0, 0.0,    0.0, 0.0, scale, 0.0,    0.0,        0.0,        0.0,        1.0);"+
                            "mat4 z = mat4(zoom,     0.0, 0.0, 0.0,    0.0, zoom,   0.0, 0.0,    0.0, 0.0, zoom,   0.0,    0.0,        0.0,       0.0,        1.0);"+
                            //"mat4 yrot =    mat4( cos(rotation.y), 0.0, - sin(rotation.y), 0.0,    0.0, 1.0, 0.0, 0.0,    sin(rotation.y), 0.0, cos(rotation.y), 0.0,    0.0, 0.0, 0.0, 1.0);"+
                            //"mat4 xrot =    mat4( 1.0, 0.0, 0.0, 0.0,    0.0, cos(rotation.x), sin(rotation.x), 0.0,    0.0, - sin(rotation.x), cos(rotation.x), 0.0,    0.0, 0.0, 0.0, 1.0);"+
                            //"mat4 rot = xrot * yrot;"+
                    "  gl_Position =  z* rot * trans  * scaling *vec4(vertex.x, vertex.y, vertex.z, 1.0);" +
                    "if(zClamp > 0.0) { gl_Position.z = min(max(gl_Position.z, -1.0), 1.0); }"+
                    "}";

    private final String cubeFSSrc  =
            "precision mediump float;" +
                    "uniform vec4 color;" +
                    "void main() {" +
                    "  gl_FragColor = color;" +
                    "}";

    private int vertexVBO;
    private int indicesVBO;
    private int outlineIndicesVBO;
    private int wireframeIndicesVBO;

    private static GLCube instance;
    public GLCube()
    {

    }

    public void init() {

        float[] cubeVertex = new float[]{
                -1.0f, -1.0f, 1.0f,  //Bottom Left   0
                1.0f, -1.0f, 1.0f,  //Bottom Right  1
                1.0f,  1.0f, 1.0f,  //Top Right     2
                -1.0f,  1.0f, 1.0f,  //Top left      3

                -1.0f, -1.0f, -1.0f, //Bottom Left   4
                1.0f, -1.0f, -1.0f, //Bottom Right  5
                1.0f,  1.0f, -1.0f, //Top Right     6
                -1.0f,  1.0f, -1.0f  //Top left      7
        };

        byte[] cubeIndices = new byte[]{
                0,1,2, 0,2,3,//front
                0,3,7, 0,7,4,//Left
                5,1,0, 4,5,0,//Bottom

                4,7,6, 5,4,6,//Back
                6,7,3, 6,3,2,//top
                6,2,1, 6,1,5//right
        };

        byte[] outlineIndices = new byte[]{
                0,1, 1,2, 2,3, 3,0, //front
                0,3, 3,7, 7,4, 4,0,//Left
                5,1, 1,0, 0,4, 4,5,//Bottom

                4,7, 7,6, 6,5, 5,4,//Back
                6,7, 7,3, 3,2, 2,6,//top
                6,2, 2,1, 1,5, 5,6//right
        };

        byte[] wireframeIndices = new byte[]{
                0, 1, 1, 2, 2, 0, 2, 3, 3, 0, //front
                0, 1, 1, 4, 4, 0, 4, 5, 5, 1, //top
                1, 5, 5, 6, 6, 1, 6, 2, 2, 1, //left
                0, 4, 4, 7, 7, 0, 7, 3, 3, 0, //right
                2, 6, 6, 7, 7, 2, 7, 3, 3, 2, //bot
                4, 5, 5, 6, 6, 4, 6, 7, 7, 4  //back
        };
        int[] vbos = new int[4];
        glGenBuffers(4,  vbos, 0);
        this.vertexVBO = vbos[0];
        this.indicesVBO = vbos[1];
        this.outlineIndicesVBO = vbos[2];
        this.wireframeIndicesVBO = vbos[3];

        ByteBuffer vertBB = ByteBuffer.allocateDirect((cubeVertex.length * 4)); // a float is 4 bytes
        vertBB.order(ByteOrder.nativeOrder()); // use native order for bytes
        FloatBuffer vertexBuffer = vertBB.asFloatBuffer(); // make a float buffer out of the byte buffer (it's the only way to directly allocate a float buffer -.-' i really do hate java)
        vertexBuffer.put(cubeVertex); // finally put our data in (seriously. in good languages i could just use my array...)
        vertexBuffer.position(0); // reset position
        glBindBuffer(GL_ARRAY_BUFFER, this.vertexVBO);
        glBufferData(GL_ARRAY_BUFFER, cubeVertex.length * 4, vertexBuffer, GL_STATIC_DRAW);

        ByteBuffer indicesBuffer = ByteBuffer.allocateDirect((cubeIndices.length));
        indicesBuffer.order(ByteOrder.nativeOrder());
        indicesBuffer.put(cubeIndices);
        indicesBuffer.position(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, cubeIndices.length, indicesBuffer, GL_STATIC_DRAW);

        ByteBuffer outlineIndicesBuffer = ByteBuffer.allocateDirect((outlineIndices.length));
        outlineIndicesBuffer.order(ByteOrder.nativeOrder());
        outlineIndicesBuffer.put(outlineIndices);
        outlineIndicesBuffer.position(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.outlineIndicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, outlineIndices.length, outlineIndicesBuffer, GL_STATIC_DRAW);

        ByteBuffer wireframeIndicesBuffer = ByteBuffer.allocateDirect((wireframeIndices.length));
        wireframeIndicesBuffer.order(ByteOrder.nativeOrder());
        wireframeIndicesBuffer.put(wireframeIndices);
        wireframeIndicesBuffer.position(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.wireframeIndicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, wireframeIndices.length, wireframeIndicesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);




        int vShader = loadShader(GL_VERTEX_SHADER, this.cubeVSSrc);
        int fShader = loadShader(GL_FRAGMENT_SHADER, this.cubeFSSrc);

        this.shader = glCreateProgram();
        glAttachShader(this.shader, vShader);
        glAttachShader(this.shader, fShader);

        glLinkProgram(this.shader);
        glValidateProgram(this.shader);
        System.out.println("Link Error: "+ gluErrorString(glGetError()));

        int[] linkStatus = new int[1];
        glGetProgramiv(this.shader, GLES20.GL_LINK_STATUS, linkStatus, 0);
        System.out.println("Validate:" + ( linkStatus[0] == GL_TRUE));

        if(linkStatus[0] == GL_FALSE)
        {

            System.out.println("InfoLog:" + glGetProgramInfoLog(this.shader));
        }

    }

    private int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = glCreateShader(type);

        // add the source code to the shader and compile it
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);

        int err = glGetError();
        System.out.println("Compile Error: "+ gluErrorString(err));


        if(err != GL_NO_ERROR) {
            System.out.println("Shader info log:" + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    //singelton, since this class is not really used as an object, but more to outsource stuff.
    //gives us all control in the renderer, without having to much code in the renderer class
    public static GLCube getInstance () {
        if (GLCube.instance == null) {
            GLCube.instance = new GLCube ();
        }
        return GLCube.instance;
    }

    //initialize all uniform locations with -1. so we know we don't know where they are yet. it helps to query them only once cause it is pretty costly.
    //although in the end it doesn't even matter in this project
    private int colorUniformLocation = -1;
    private int positionUniformLocation = -1;
    private int scaleUniformLocation = -1;

    private int centerUniformLocation = -1;
    private int rotationUniformLocation = -1;

    private int vertexAttribLocation = -1;
    private int zoomUniformLocation = -1;


    private int zClampUniformLocation = -1;

    private static final int POINTS_PER_VERTEX = 3;

    public void drawAt(float x, float y, float z, float xrot, float yrot, float zrot, float cx, float cy, float cz, float scale, float zoom)
    {

        glUseProgram(this.shader);
        if(this.colorUniformLocation == -1)
            this.colorUniformLocation = glGetUniformLocation(this.shader, "color");

        if(this.positionUniformLocation == -1)
            this.positionUniformLocation = glGetUniformLocation(this.shader, "position");

        if(this.scaleUniformLocation == -1)
            this.scaleUniformLocation = glGetUniformLocation(this.shader, "scale");

        if(this.centerUniformLocation == -1)
            this.centerUniformLocation = glGetUniformLocation(this.shader, "center");

        if(this.rotationUniformLocation == -1)
            this.rotationUniformLocation = glGetUniformLocation(this.shader, "rotation");

        if(this.zoomUniformLocation == -1)
            this.zoomUniformLocation = glGetUniformLocation(this.shader, "zoom");

        if(this.zClampUniformLocation == -1)
            this.zClampUniformLocation = glGetUniformLocation(this.shader, "zClamp");

        if(this.vertexAttribLocation == -1)
            this.vertexAttribLocation = glGetAttribLocation(this.shader,"vertex");


        glUniform4f(this.colorUniformLocation, 1.0f, 1.0f, 1.0f, 0.8f);
        glUniform3f(this.positionUniformLocation, x,y,z);

        //glUniform3f(this.rotationUniformLocation, xrot,yrot,zrot);

        //glUniform3f(this.centerUniformLocation, cx,cy,cz);
        //glUniform1f(this.scaleUniformLocation, scale);
        //glUniform1f(this.zoomUniformLocation, zoom);

        glUniform1f(this.zClampUniformLocation, 0.0f);


        glBindBuffer(GL_ARRAY_BUFFER, this.vertexVBO);
        glEnableVertexAttribArray(this.vertexAttribLocation);
        glVertexAttribPointer(this.vertexAttribLocation, POINTS_PER_VERTEX, GL_FLOAT, false, 0,0);

        glDepthFunc((GL_ALWAYS));

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indicesVBO);
        /*
        glUniform4f(this.colorUniformLocation, 1.0f, 0.0f, 0.0f, 0.6f);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 0); // FRONT

        glUniform4f(this.colorUniformLocation, 0.0f, 1.0f, 0.0f, 0.6f);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 6); // LEFT

        glUniform4f(this.colorUniformLocation, 0.0f, 0.0f, 1.0f, 0.6f);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 12); // BOT

        glUniform4f(this.colorUniformLocation, 1.0f, 1.0f, 0.0f, 0.6f);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 18); // BACK

        glUniform4f(this.colorUniformLocation, 1.0f, 0.0f, 1.0f, 0.6f);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 24); // TOP

        glUniform4f(this.colorUniformLocation, 0.0f, 1.0f, 1.0f, 0.6f);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 30); // RIGHT
        */
        glUniform4f(this.colorUniformLocation, this.cubeColor.getR(), this.cubeColor.getG(), this.cubeColor.getB(), this.cubeColor.getA());


        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, 0);
        glUniform4f(this.colorUniformLocation, this.cubeOutlineColor.getR(), this.cubeOutlineColor.getG(), this.cubeOutlineColor.getB(), this.cubeOutlineColor.getA());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.outlineIndicesVBO);
        glLineWidth(100.0f * scale);
        glDepthFunc( GL_GEQUAL);
        glDrawElements(GL_LINES, 48, GL_UNSIGNED_BYTE, 0);

        //glUniform1f(this.scaleUniformLocation, scale + 0.001f);
        //glDrawArrays(GL_LINES, 0, 36);


    }

    public void drawOutlineAt(float x, float y, float z, float xrot, float yrot, float zrot, float cx, float cy, float cz, float scale, float zoom)
    {

        glUseProgram(this.shader);


        if(this.colorUniformLocation == -1)
            this.colorUniformLocation = glGetUniformLocation(this.shader, "color");

        if(this.positionUniformLocation == -1)
            this.positionUniformLocation = glGetUniformLocation(this.shader, "position");

        if(this.scaleUniformLocation == -1)
            this.scaleUniformLocation = glGetUniformLocation(this.shader, "scale");

        if(this.centerUniformLocation == -1)
            this.centerUniformLocation = glGetUniformLocation(this.shader, "center");

        if(this.rotationUniformLocation == -1)
            this.rotationUniformLocation = glGetUniformLocation(this.shader, "rotation");

        if(this.zoomUniformLocation == -1)
            this.zoomUniformLocation = glGetUniformLocation(this.shader, "zoom");


        if(this.zClampUniformLocation == -1)
            this.zClampUniformLocation = glGetUniformLocation(this.shader, "zClamp");

        if(this.vertexAttribLocation == -1)
            this.vertexAttribLocation = glGetAttribLocation(this.shader,"vertex");


        glUniform3f(this.positionUniformLocation, x,y,z);

        glUniform3f(this.rotationUniformLocation, xrot,yrot,zrot);

        glUniform3f(this.centerUniformLocation, cx,cy,cz);
        glUniform1f(this.scaleUniformLocation, scale);
        glUniform1f(this.zoomUniformLocation, zoom);

        glUniform1f(this.zClampUniformLocation, 1.0f);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexVBO);
        glEnableVertexAttribArray(this.vertexAttribLocation);
        glVertexAttribPointer(this.vertexAttribLocation, POINTS_PER_VERTEX, GL_FLOAT, false, 0,0);

        glUniform4f(this.colorUniformLocation, this.singleOutlineColor.getR(), this.singleOutlineColor.getG(), this.singleOutlineColor.getB(), this.singleOutlineColor.getA());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.outlineIndicesVBO);
        glLineWidth(6.0f);

        glDepthFunc( GL_ALWAYS);
        glDrawElements(GL_LINES, 48, GL_UNSIGNED_BYTE, 0);

        //glUniform1f(this.scaleUniformLocation, scale + 0.001f);
        //glDrawArrays(GL_LINES, 0, 36);


    }



    private float byteColorToGLColor(int i) { return (1.0f/255.0f) * (float)i;}

    private Vector4 cubeColor = new Vector4(1.0f, 1.0f, 1.0f, 0.8f);
    private Vector4 cubeOutlineColor = new Vector4(0.0f, 0.0f, 0.0f, 1.0f);
    private Vector4 singleOutlineColor = new Vector4(0.8f, 0.8f, 0.8f, 2.0f);

    /**
     * Gets the color of the cube faces
     *
     * @return a Vector4 representing the cube color; in GL format
     * */
    public Vector4 getCubeColor() { return this.cubeColor; }

    /**
     * Sets the color of the cube faces
     *
     * @param  cubeColor a Vector4 representing the cube color; in GL format
     * */
    public void setCubeColor(Vector4 cubeColor) { this.cubeColor = cubeColor; }

    /**
     * Sets the color of the cube faces in GL format, e.g. every channel has values ranging from 0.0 to 1.0
     *
     * @param  r representing the red component of the color; in GL format
     * @param  g representing the green component of the color; in GL format
     * @param  b representing the blue component of the color; in GL format
     * */
    public void setCubeColor(float r, float g, float b) { this.cubeColor = new Vector4(r,g,b, 1.0f); }

    /**
     * Sets the color of the cube faces in GL format, e.g. every channel has values ranging from 0.0 to 1.0
     *
     * @param  r representing the red component of the color; in GL format
     * @param  g representing the green component of the color; in GL format
     * @param  b representing the blue component of the color; in GL format
     * @param  a representing the alpha component of the color; in GL format
     * */
    public void setCubeColor(float r, float g ,float b, float a) { this.cubeColor = new Vector4(r,g,b,a); }

    /**
     * Sets the color of the cube faces in a more sane (byte) format, e.g. every channel has values ranging from 0 to 255
     *
     * @param  r representing the red component of the color; in byte format
     * @param  g representing the green component of the color; in byte format
     * @param  b representing the blue component of the color; in byte format
     * */
    public void setCubeColor(int r, int g, int b) { this.cubeColor = new Vector4(byteColorToGLColor(r),byteColorToGLColor(g),byteColorToGLColor(b), 1.0f); }

    /**
     * Sets the color of the cube faces in a more sane (byte) format, e.g. every channel has values ranging from 0 to 255
     *
     * @param  r representing the red component of the color; in byte format
     * @param  g representing the green component of the color; in byte format
     * @param  b representing the blue component of the color; in byte format
     * @param  a representing the alpha component of the color; in byte format
     * */
    public void setCubeColor(int r, int g ,int b, int a) { this.cubeColor = new Vector4(byteColorToGLColor(r),byteColorToGLColor(g),byteColorToGLColor(b), byteColorToGLColor(a)); }

    /**
     * Gets the color of the cube outlines
     *
     * @return a Vector4 representing the cube outline color; in GL format
     * */
    public Vector4 getCubeOutlineColor() { return this.cubeOutlineColor; }

    /**
     * Sets the color of the cube outlines
     *
     * @param  cubeOutlineColor a Vector4 representing the cube outline color; in GL format
     * */
    public void setCubeOutlineColor(Vector4 cubeOutlineColor) { this.cubeOutlineColor = cubeOutlineColor; }

    /**
     * Sets the color of the cube outlines in GL format, e.g. every channel has values ranging from 0.0 to 1.0
     *
     * @param  r representing the red component of the color; in GL format
     * @param  g representing the green component of the color; in GL format
     * @param  b representing the blue component of the color; in GL format
     * */
    public void setCubeOutlineColor(float r, float g, float b) { this.cubeOutlineColor = new Vector4(r,g,b, 1.0f); }

    /**
     * Sets the color of the cube outlines in GL format, e.g. every channel has values ranging from 0.0 to 1.0
     *
     * @param  r representing the red component of the color; in GL format
     * @param  g representing the green component of the color; in GL format
     * @param  b representing the blue component of the color; in GL format
     * @param  a representing the alpha component of the color; in GL format
     * */
    public void setCubeOutlineColor(float r, float g ,float b, float a) { this.cubeOutlineColor = new Vector4(r,g,b,a); }

    /**
     * Sets the color of the cube outlines in a more sane (byte) format, e.g. every channel has values ranging from 0 to 255
     *
     * @param  r representing the red component of the color; in byte format
     * @param  g representing the green component of the color; in byte format
     * @param  b representing the blue component of the color; in byte format
     * */
    public void setCubeOutlineColor(int r, int g, int b) { this.cubeOutlineColor = new Vector4(byteColorToGLColor(r),byteColorToGLColor(g),byteColorToGLColor(b), 1.0f); }

    /**
     * Sets the color of the cube outlines in a more sane (byte) format, e.g. every channel has values ranging from 0 to 255
     *
     * @param  r representing the red component of the color; in byte format
     * @param  g representing the green component of the color; in byte format
     * @param  b representing the blue component of the color; in byte format
     * @param  a representing the alpha component of the color; in byte format
     * */
    public void setCubeOutlineColor(int r, int g ,int b, int a) { this.cubeOutlineColor = new Vector4(byteColorToGLColor(r),byteColorToGLColor(g),byteColorToGLColor(b), byteColorToGLColor(a)); }
    /**
     * Sets the outline color if drawn as a standalone
     *
     * @return  a Vector4 representing the outline color if drawn as a standalone; in GL format
     * */

    public Vector4 getSingleOutlineColor() { return this.singleOutlineColor; }
    /**
     * Sets the outline color if drawn as a standalone
     *
     * @param  singleOutlineColor a Vector4 representing the outline color if drawn as a standalone; in GL format
     * */
    public void setSingleOutlineColor(Vector4 singleOutlineColor) { this.singleOutlineColor = singleOutlineColor; }

    /**
     * Sets the color of the outline drawn as a standalone in GL format, e.g. every channel has values ranging from 0.0 to 1.0
     *
     * @param  r representing the red component of the color; in GL format
     * @param  g representing the green component of the color; in GL format
     * @param  b representing the blue component of the color; in GL format
     * */
    public void setSingleOutlineColor(float r, float g, float b) { this.singleOutlineColor = new Vector4(r,g,b, 1.0f); }

    /**
     * Sets the color of the outline drawn as a standalone in GL format, e.g. every channel has values ranging from 0.0 to 1.0
     *
     * @param  r representing the red component of the color; in GL format
     * @param  g representing the green component of the color; in GL format
     * @param  b representing the blue component of the color; in GL format
     * @param  a representing the alpha component of the color; in GL format
     * */
    public void setSingleOutlineColor(float r, float g ,float b, float a) { this.singleOutlineColor = new Vector4(r,g,b,a); }

    /**
     * Sets the color of the outline drawn as a standalone in a more sane (byte) format, e.g. every channel has values ranging from 0 to 255
     *
     * @param  r representing the red component of the color; in byte format
     * @param  g representing the green component of the color; in byte format
     * @param  b representing the blue component of the color; in byte format
     * */
    public void setSingleOutlineColor(int r, int g, int b) { this.singleOutlineColor = new Vector4(byteColorToGLColor(r),byteColorToGLColor(g),byteColorToGLColor(b), 1.0f); }

    /**
     * Sets the color of the outline drawn as a standalone in a more sane (byte) format, e.g. every channel has values ranging from 0 to 255
     *
     * @param  r representing the red component of the color; in byte format
     * @param  g representing the green component of the color; in byte format
     * @param  b representing the blue component of the color; in byte format
     * @param  a representing the alpha component of the color; in byte format
     * */
    public void setSingleOutlineColor(int r, int g ,int b, int a) { this.singleOutlineColor = new Vector4(byteColorToGLColor(r),byteColorToGLColor(g),byteColorToGLColor(b), byteColorToGLColor(a)); }

}
