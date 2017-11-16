package ekg.visualizer;

/**
 * Created by Josh on 15.11.2017.
 */

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_ALWAYS;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FALSE;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_GEQUAL;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRUE;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glValidateProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLU.gluErrorString;
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

public class GLAxisSystem {

    private int shader;
    public int getShader() {
        return shader;
    }


    private final String cubeVSSrc =
            "attribute vec4 vertex;" +
                    "uniform vec3 center;"+
                    "uniform vec3 rotation;"+
                    "uniform float scale;"+
                    "uniform float zoom;"+
                    "void main() {" +
                        "float yang = 0.0;"+//785;"+
                        "float xang = 0.0;"+
                        "mat4 trans = mat4(1.0,     0.0, 0.0, 0.0,    0.0, 1.0,   0.0, 0.0,    0.0, 0.0, 1.0,   0.0,   -1.0,       -1.0,       -1.0,        1.0);"+
                        "mat4 z = mat4(zoom, 0.0, 0.0, 0.0,    0.0, zoom, 0.0, 0.0,    0.0, 0.0, zoom, 0.0,    0.0,        0.0,        0.0,        1.0);"+
                        "mat4 yrot =    mat4( cos(rotation.y), 0.0, - sin(rotation.y), 0.0,    0.0, 1.0, 0.0, 0.0,    sin(rotation.y), 0.0, cos(rotation.y), 0.0,    0.0, 0.0, 0.0, 1.0);"+
                        "mat4 xrot =    mat4( 1.0, 0.0, 0.0, 0.0,    0.0, cos(rotation.x), sin(rotation.x), 0.0,    0.0, - sin(rotation.x), cos(rotation.x), 0.0,    0.0, 0.0, 0.0, 1.0);"+
                        "mat4 rot = xrot * yrot;"+
                    "  gl_Position =z * rot *   trans  *vec4(vertex.x, vertex.y, vertex.z, 1.0);" +
                    "gl_Position.z = 1.0;"+
                    "}";

    private final String cubeFSSrc  =
            "precision mediump float;" +
                    "uniform vec4 color;" +
                    "void main() {" +
                    "  gl_FragColor = color;" +
                    "}";

    private int vertexVBO;
    private int indicesVBO;

    private static GLAxisSystem instance;
    public GLAxisSystem()
    {

        float[] cubeVertex = new float[]{
                -10.0f,    0.0f,   0.0f,  1.0f, //X axis left     0
                 10.0f,    0.0f,   0.0f,  1.0f, //X axis right    1

                  0.0f,  -100.0f,   0.0f,  1.0f, //Y axis bottom   2
                  0.0f,   100.0f,   0.0f,  1.0f, //Y axis top      3

                  0.0f,    0.0f, -100.0f,  1.0f, //Z axis front    4
                  0.0f,     0.0f, 100.0f,  1.0f, //Z axis back     5
        };

        byte[] cubeIndices = new byte[]{

                0,1, //X axis
                2,3, //Y axis
                4,5  //Z axis
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
    public static GLAxisSystem getInstance () {
        if (GLAxisSystem.instance == null) {
            GLAxisSystem.instance = new GLAxisSystem ();
        }
        return GLAxisSystem.instance;
    }

    //initialize all uniform locations with -1. so we know we don't know where they are yet. it helps to query them only once cause it is pretty costly.
    //although in the end it doesn't even matter in this project
    private int colorUniformLocation = -1;
    private int positionUniformLocation = -1;
    private int scaleUniformLocation = -1;

    private int centerUniformLocation = -1;
    private int rotationUniformLocation = -1;

    private int vertexAttribLocation = -1;

    private int zoomAttribLocation = -1;

    private static final int POINTS_PER_VERTEX = 4;



    public void drawAt(float xrot, float yrot, float zrot, float cx, float cy, float cz, float scale, float zoom)
    {

        glUseProgram(this.shader);


        if(this.colorUniformLocation == -1)
            this.colorUniformLocation = glGetUniformLocation(this.shader, "color");

        if(this.scaleUniformLocation == -1)
            this.scaleUniformLocation = glGetUniformLocation(this.shader, "scale");

        if(this.centerUniformLocation == -1)
            this.centerUniformLocation = glGetUniformLocation(this.shader, "center");

        if(this.rotationUniformLocation == -1)
            this.rotationUniformLocation = glGetUniformLocation(this.shader, "rotation");

        if(this.zoomAttribLocation == -1)
            this.zoomAttribLocation = glGetUniformLocation(this.shader, "zoom");

        if(this.vertexAttribLocation == -1)
            this.vertexAttribLocation = glGetAttribLocation(this.shader,"vertex");


        //glUniform3f(this.positionUniformLocation, x,y,z);
        glUniform1f(this.scaleUniformLocation, scale);
        glUniform1f(this.zoomAttribLocation, zoom);
        glUniform3f(this.rotationUniformLocation, xrot,yrot,zrot);

        glUniform3f(this.centerUniformLocation, cx,cy,cz);
        glBindBuffer(GL_ARRAY_BUFFER, this.vertexVBO);
        glEnableVertexAttribArray(this.vertexAttribLocation);
        glVertexAttribPointer(this.vertexAttribLocation, POINTS_PER_VERTEX, GL_FLOAT, false, 0,0);

        glDepthFunc((GL_ALWAYS));
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indicesVBO);

        glDepthRangef(0.01f, 1000.0f);

        glUniform4f(this.colorUniformLocation, 1.0f, 0.0f, 0.0f, 1.0f);

        glLineWidth(200.0f * scale);
        glDrawElements(GL_LINES, 2, GL_UNSIGNED_BYTE, 0);

        glUniform4f(this.colorUniformLocation, 0.0f, 1.0f, 0.0f, 1.0f);
        glDrawElements(GL_LINES, 2, GL_UNSIGNED_BYTE, 2);

        glUniform4f(this.colorUniformLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawElements(GL_LINES, 2, GL_UNSIGNED_BYTE, 4);

        //glUniform1f(this.scaleUniformLocation, scale + 0.001f);
        //glDrawArrays(GL_LINES, 0, 36);


    }
}

