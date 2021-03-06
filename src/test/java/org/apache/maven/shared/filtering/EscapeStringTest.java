package org.apache.maven.shared.filtering;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Resource;
import org.apache.maven.shared.utils.io.FileUtils;
import org.apache.maven.shared.utils.io.IOUtil;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Olivier Lamy
 */
public class EscapeStringTest
    extends PlexusTestCase
{

    File outputDirectory = new File( getBasedir(), "target/EscapeStringTest" );

    File unitDirectory = new File( getBasedir(), "src/test/units-files/escape-remove-char" );

    protected void setUp()
        throws Exception
    {
        super.setUp();
        if ( outputDirectory.exists() )
        {
            FileUtils.forceDelete( outputDirectory );
        }
        outputDirectory.mkdirs();
    }

    public void testEscape()
        throws Exception
    {
        File baseDir = new File( "c:\\foo\\bar" );
        StubMavenProject mavenProject = new StubMavenProject( baseDir );
        mavenProject.setVersion( "1.0" );
        mavenProject.setGroupId( "org.apache" );
        mavenProject.setName( "test project" );

        Properties projectProperties = new Properties();
        projectProperties.put( "foo", "bar" );
        projectProperties.put( "java.version", "zloug" );
        projectProperties.put( "replaceThis", "I am the replacement" );
        mavenProject.setProperties( projectProperties );
        MavenResourcesFiltering mavenResourcesFiltering = lookup( MavenResourcesFiltering.class );

        Resource resource = new Resource();
        List<Resource> resources = new ArrayList<Resource>();
        resources.add( resource );
        resource.setDirectory( unitDirectory.getPath() );
        resource.setFiltering( true );

        List<String> filtersFile = new ArrayList<String>();

        List<String> nonFilteredFileExtensions = Collections.singletonList( "gif" );

        MavenResourcesExecution mavenResourcesExecution =
            new MavenResourcesExecution( resources, outputDirectory, mavenProject, "UTF-8", filtersFile,
                                         nonFilteredFileExtensions, new StubMavenSession() );
        mavenResourcesExecution.setUseDefaultFilterWrappers( true );

        mavenResourcesExecution.setEscapeString( "!" );

        mavenResourcesFiltering.filterResources( mavenResourcesExecution );

        FileInputStream in = null;
        try
        {
            in = new FileInputStream( new File( outputDirectory, "content.xml" ) );
            String content = IOUtil.toString( in );
            in.close();
            in = null;
            assertTrue( content.contains( "<broken-tag>Content with replacement: I am the replacement !</broken-tag>" ) );
            assertTrue( content.contains( "<broken-tag>Content with escaped replacement: Do not ${replaceThis} !</broken-tag>" ) );
        }
        finally
        {
            IOUtil.close( in );
        }

    }
}
