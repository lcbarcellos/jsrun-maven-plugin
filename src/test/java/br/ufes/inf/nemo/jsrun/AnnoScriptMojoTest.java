package br.ufes.inf.nemo.jsrun;

import br.ufes.inf.nemo.jsrun.AnnotationMojo;
import static br.ufes.inf.nemo.jsrun.Constants.BASE_DIR;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

public class AnnoScriptMojoTest {

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething() throws Exception {
        File pom = new File("target/test-classes/project-to-test/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        AnnotationMojo myMojo = (AnnotationMojo) rule.lookupConfiguredMojo(pom, "process");
        assertNotNull(myMojo);


        File outputDirectory = ( File ) rule.getVariableValueFromObject( myMojo, "outputDirectory" );
        assertNotNull( outputDirectory );

        File baseDir = ( File ) rule.getVariableValueFromObject( myMojo, "baseDir" );
        assertNotNull( baseDir );


//        myMojo.execute();

        //assertFalse(BASE_DIR.isNull());
        //assertEquals(BASE_DIR.get(), pom.getAbsolutePath());
    }

    /**
     * Do not need the MojoRule.
     */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
        assertTrue(true);
    }

}
