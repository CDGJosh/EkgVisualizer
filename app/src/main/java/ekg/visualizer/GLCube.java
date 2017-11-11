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
                    "uniform float scale;"+
                            "uniform vec4 color;" +
                    "void main() {" +
                            "mat4 trans = mat4(1.0, 0.0, 0.0, position.x,    0.0, 1.0, 0.0, position.y,    0.0, 0.0, 1.0, position.z,    0.0, 0.0, 0.0, 1.0);"+
                    "  gl_Position = trans * vec4(vertex.x, vertex.y, vertex.z, 1.0);" +
                    "}";

    private final String cubeFSSrc  =
            "precision mediump float;" +
                    "uniform vec4 color;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);" +
                    "}";

    private int vertexVBO;
    private int indicesVBO;

    private static GLCube instance;
    public GLCube()
    {

        float[] cubeVertex = new float[]{
                1.0f, 1.0f, 1.0f,    //front top right  0
               -1.0f, 1.0f, 1.0f,    //front top left   1
               -1.0f, -1.0f, 1.0f,   //front bot left   2
                1.0f, -1.0f, 1.0f,   //front bot right  3
                1.0f, 1.0f, -1.0f,   //back top right   4
                -1.0f, 1.0f, -1.0f,  //back top left    5
                -1.0f, -1.0f, -1.0f, //back bot left    6
                1.0f, -1.0f, -1.0f   //back bot right   7
        };

        byte[] cubeIndices = new byte[]{
                0, 1, 2, 2, 3, 0, //front
                0, 1, 4, 4, 5, 0, //top
                1, 5, 6, 6, 2, 1, //left
                0, 4, 7, 7, 3, 0, //right
                2, 6, 7, 7, 3, 2, //bot
                4, 5, 6, 6, 7, 4  //back
        };
        int[] vbos = new int[2];
        glGenBuffers(2,  vbos, 0);
        this.vertexVBO = vbos[0];
        this.indicesVBO = vbos[1];

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

    private int vertexAttribLocation = -1;

    private static final int POINTS_PER_VERTEX = 3;

    public void drawAt(float x, float y, float z, float scale)
    {
        this.drawAt(x,y,z,0,0,0,0,0,0,scale);
    }

    public void drawAt(float x, float y, float z, float xrot, float yrot, float zrot, float cx, float cy, float cz, float scale)
    {

        glUseProgram(this.shader);


        if(this.colorUniformLocation == -1)
            this.colorUniformLocation = glGetUniformLocation(this.shader, "color");

        if(this.positionUniformLocation == -1)
            this.positionUniformLocation = glGetUniformLocation(this.shader, "position");

        if(this.scaleUniformLocation == -1)
            this.scaleUniformLocation = glGetUniformLocation(this.shader, "scale");

        if(this.vertexAttribLocation == -1)
            this.vertexAttribLocation = glGetAttribLocation(this.shader,"vertex");


        glUniform4f(this.colorUniformLocation, 1.0f, 1.0f, 1.0f, 0.8f);
        glUniform3f(this.positionUniformLocation, x,y,z);
        glUniform1f(this.scaleUniformLocation, scale);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexVBO);
        glEnableVertexAttribArray(this.vertexAttribLocation);
        glVertexAttribPointer(this.vertexAttribLocation, POINTS_PER_VERTEX, GL_FLOAT, false, 0,0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indicesVBO);

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 0);


    }
}
