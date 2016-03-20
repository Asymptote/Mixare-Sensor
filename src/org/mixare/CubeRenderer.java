package org.mixare;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Render a cube.
 */
class CubeRenderer implements GLSurfaceView.Renderer {

    private Cube mCube;
    private float pitch = 0.0F;
    private float roll = 0.0F;
    private float azimuth = 0.0F;


    public CubeRenderer() {

        mCube = new Cube();
    }

    public void onDrawFrame(GL10 gl) {


        /* clear screen*/
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /* set MatrixMode to model view*/
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glLoadIdentity();

        gl.glRotatef(-0, 0, 0, 1);
        gl.glRotatef(-0,0,1,0);
        gl.glRotatef(-roll,1,0,0);
        gl.glTranslatef(-0, -2, -0f);

        gl.glTranslatef(-pitch, 0, -8f);
        gl.glRotatef(0, 1, 0, 0);
        gl.glRotatef(0,0,1,0);
        gl.glRotatef(0,0,0,1);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        //GLU.gluLookAt(gl, 0.0F, 2.0F, 0.0F,pitch,roll, 0.0F, 0.0F,1.0F,0.0F);

        mCube.draw(gl);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);


     /*
      * Set our projection matrix. This doesn't have to be done
      * each time we draw, but usually a new projection needs to
      * be set when the viewport is resized.
      */

        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
     /*
     * By default, OpenGL enables features that improve quality
     * but reduce performance. One might want to tweak that
     * especially on software renderer.
     */
        gl.glDisable(GL10.GL_DITHER);

    /*
     * Some one-time OpenGL initialization can be made here
     * probably based on features of this particular context
     */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_FASTEST);


            gl.glClearColor(0, 0, 0, 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);

    }



    public void setAzimuth(Float azimuth){
        this.azimuth = azimuth;
    }

    public void setRoll(Float roll){
        this.roll = roll;
    }

    public void setPitch(Float pitch){
        this.pitch = pitch;
    }
}
