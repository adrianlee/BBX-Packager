/*
 * Copyright 2010-2011 Research In Motion Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.rim.tumbler.serialize;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import junit.framework.Assert;
import net.rim.tumbler.config.IWidgetConfig;
import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.json4j.JSONArray;
import net.rim.tumbler.json4j.JSONException;
import net.rim.tumbler.json4j.JSONObject;
import net.rim.tumbler.session.SessionManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Test;

/**
 * JUnit for WidgetConfig_v1Serializer.
 * 
 * Given values parsed from XML (by mocking WidgetConfig), test if the serializer writes correct values to the JSON object
 */
public class SerializerTest {
    private Mockery _context = new JUnit4Mockery() {
        {
            setImposteriser( ClassImposteriser.INSTANCE );
        }
    };

    private IWidgetConfig _widgetConfig;
    private WidgetConfig_v1Serializer _serializer;
    
    private static final String TLD = "$$ac$$ad$$ae$$aero$$af$$ag$$ai$$al$$am$$an$$ao$$aq$$ar$$arpa$$as$$asia$$at$$au$$aw$$ax$$az$$ba$$bb$$bd$$be$$bf$$bg$$bh$$bi$$biz$$bj$$bm$$bn$$bo$$br$$bs$$bt$$bv$$bw$$by$$bz$$ca$$cat$$cc$$cd$$cf$$cg$$ch$$ci$$ck$$cl$$cm$$cn$$co$$com$$coop$$cr$$cu$$cv$$cx$$cy$$cz$$de$$dj$$dk$$dm$$do$$dz$$ec$$edu$$ee$$eg$$er$$es$$et$$eu$$fi$$fj$$fk$$fm$$fo$$fr$$ga$$gb$$gd$$ge$$gf$$gg$$gh$$gi$$gl$$gm$$gn$$gov$$gp$$gq$$gr$$gs$$gt$$gu$$gw$$gy$$hk$$hm$$hn$$hr$$ht$$hu$$id$$ie$$il$$im$$in$$info$$int$$io$$iq$$ir$$is$$it$$je$$jm$$jo$$jobs$$jp$$ke$$kg$$kh$$ki$$km$$kn$$kp$$kr$$kw$$ky$$kz$$la$$lb$$lc$$li$$lk$$lr$$ls$$lt$$lu$$lv$$ly$$ma$$mc$$md$$me$$mg$$mh$$mil$$mk$$ml$$mm$$mn$$mo$$mobi$$mp$$mq$$mr$$ms$$mt$$mu$$museum$$mv$$mw$$mx$$my$$mz$$na$$name$$nc$$ne$$net$$nf$$ng$$ni$$nl$$no$$np$$nr$$nu$$nz$$om$$org$$pa$$pe$$pf$$pg$$ph$$pk$$pl$$pm$$pn$$pr$$pro$$ps$$pt$$pw$$py$$qa$$re$$ro$$rs$$ru$$rw$$sa$$sb$$sc$$sd$$se$$sg$$sh$$si$$sj$$sk$$sl$$sm$$sn$$so$$sr$$st$$su$$sv$$sy$$sz$$tc$$td$$tel$$tf$$tg$$th$$tj$$tk$$tl$$tm$$tn$$to$$tp$$tr$$travel$$tt$$tv$$tw$$tz$$ua$$ug$$uk$$us$$uy$$uz$$va$$vc$$ve$$vg$$vi$$vn$$vu$$wf$$ws$$xn--0zwm56d$$xn--11b5bs3a9aj6g$$xn--80akhbyknj4f$$xn--9t4b11yi5a$$xn--deba0ad$$xn--g6w251d$$xn--hgbk6aj7f53bba$$xn--hlcj6aya9esc7a$$xn--jxalpdlp$$xn--kgbechtv$$xn--zckzah$$ye$$yt$$yu$$za$$zm$$zw$$";
    
    private static Hashtable< WidgetAccess, Vector< WidgetFeature >> getTestAccessTable() throws Exception {
        Hashtable< WidgetAccess, Vector< WidgetFeature >> accessTable = new Hashtable< WidgetAccess, Vector< WidgetFeature >>();

        WidgetAccess localAccess = new WidgetAccess( "WidgetConfig.WIDGET_LOCAL_DOMAIN", true );
        Vector< WidgetFeature > localFeatures = new Vector< WidgetFeature >();

        localFeatures.add( new WidgetFeature( "blackberry.system", true, "1.0.0", null ) );
        localFeatures.add( new WidgetFeature( "blackberry.app", true, "1.0.0.0", null ) );

        accessTable.put( localAccess, localFeatures );

        WidgetAccess rimAccess = new WidgetAccess( "http://test-xp.rim.net", false );
        Vector< WidgetFeature > rimFeatures = new Vector< WidgetFeature >();

        rimFeatures.add( new WidgetFeature( "blackberry.ui.dialog", true, "1.0.0", null ) );
        rimFeatures.add( new WidgetFeature( "blackberry.io.file", true, "1.0.0", null ) );
        rimFeatures.add( new WidgetFeature( "blackberry.media.microphone", true, "1.0.0", null ) );
        rimFeatures.add( new WidgetFeature( "blackberry.system", true, "1.0.0", null ) );

        accessTable.put( rimAccess, rimFeatures );

        return accessTable;
    }
    
    private static Vector< String > getIconList() {
        Vector< String > iconList = new Vector< String >();
        iconList.add( "images/icon.png" );
        iconList.add( "images/icon2.gif" );

        return iconList;
    }
    
    private static HashMap< String, String > getCustomHeaders() {
        HashMap< String, String > headers = new HashMap< String, String >();
        headers.put( "webworks", "rim/webworks" );
        headers.put( "RIM-Widget", "rim/widget" );
        return headers;
    }

    @Test
    public void testSerializeBasic() throws Exception {
        // mock WidgetConfig, take values parsed from XML for granted
        _widgetConfig = _context.mock( IWidgetConfig.class );
        _context.checking( new Expectations() {
            {
                allowing( _widgetConfig ).getName(); will( returnValue( "My App" ) );
                allowing( _widgetConfig ).getVersion(); will( returnValue( "1.0.0.0" ) );
                allowing( _widgetConfig ).getID(); will( returnValue( "MyApp" ) );
                allowing( _widgetConfig ).getDescription(); will( returnValue( "This is a very powerful app!" ) );
                allowing( _widgetConfig ).getContent(); will( returnValue( "default.html" ) );
                allowing( _widgetConfig ).getConfigXML(); will( returnValue( "config.xml" ) );
                allowing( _widgetConfig ).getBackButtonBehaviour(); will( returnValue( "back" ) );
                allowing( _widgetConfig ).getContentType(); will( returnValue( "" ) );
                allowing( _widgetConfig ).getContentCharSet(); will( returnValue( "utf-8" ) );
                allowing( _widgetConfig ).getLicense(); will( returnValue( "Apache 2.0" ) );
                allowing( _widgetConfig ).getLicenseURL(); will( returnValue( "http://www.apache.org" ) );
                allowing( _widgetConfig ).getAuthor(); will( returnValue( "John O' Conner" ) );
                allowing( _widgetConfig ).getCopyright(); will( returnValue( "Copyright (c) 2011 John O'Connor" ) );
                allowing( _widgetConfig ).getAuthorEmail(); will( returnValue( "john@helloworld.com" ) );
                allowing( _widgetConfig ).getLoadingScreenColour(); will( returnValue( "#000000" ) );
                allowing( _widgetConfig ).getBackgroundImage(); will( returnValue( "images/backgroundImg.jpg" ) );
                allowing( _widgetConfig ).getForegroundImage(); will( returnValue( "images/foregroundImg.jpg" ) );
                allowing( _widgetConfig ).getAuthorURL(); will( returnValue( "http://john.helloworld.com" ) );
                allowing( _widgetConfig ).getBackgroundSource(); will( returnValue( "background.html" ) );
                allowing( _widgetConfig ).getForegroundSource(); will( returnValue( "foreground.html" ) );
                allowing( _widgetConfig ).allowMultiAccess(); will( returnValue( true ) );
                allowing( _widgetConfig ).getIconSrc(); will( returnValue( getIconList() ) );
                allowing( _widgetConfig ).getHoverIconSrc(); will( returnValue( getIconList() ) );
                allowing( _widgetConfig ).getCustomHeaders(); will( returnValue( getCustomHeaders() ) );
                allowing( _widgetConfig ).getNavigationMode(); will( returnValue( true ) );
                allowing( _widgetConfig ).getFirstPageLoad(); will( returnValue( true ) );
                allowing( _widgetConfig ).getLocalPageLoad(); will( returnValue( true ) );
                allowing( _widgetConfig ).getRemotePageLoad(); will( returnValue( true ) );
                allowing( _widgetConfig ).isCacheEnabled(); will( returnValue( new Boolean( true ) ) );
                allowing( _widgetConfig ).getAggressiveCacheAge(); will( returnValue( new Integer( 300000 ) ) );
                allowing( _widgetConfig ).getMaxCacheSize(); will( returnValue( new Integer( 1024 ) ) );
                allowing( _widgetConfig ).getMaxCacheItemSize(); will( returnValue( new Integer( 1024 ) ) );
                allowing( _widgetConfig ).isDebugEnabled(); will( returnValue( true ) );
                allowing( _widgetConfig ).allowInvokeParams(); will( returnValue( true ) );
                allowing( _widgetConfig ).isStartupEnabled(); will( returnValue( true ) );
                allowing( _widgetConfig ).getAccessTable(); will( returnValue( new Hashtable< WidgetAccess, Vector< WidgetFeature >>() ) );
            }
        } );
        
        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();
        Assert.assertNotNull( configJSON );

        Assert.assertEquals( "My App", configJSON.getString( "name" ) );
        Assert.assertEquals( "1.0.0.0", configJSON.getString( "version" ) );
        Assert.assertEquals( "MyApp", configJSON.getString( "id" ) );
        Assert.assertEquals( "This is a very powerful app!", configJSON.getString( "description" ) );
        Assert.assertEquals( "John O' Conner", configJSON.getString( "author" ) );
        Assert.assertEquals( "http://john.helloworld.com", configJSON.getString( "authorURL" ) );
        Assert.assertEquals( "john@helloworld.com", configJSON.getString( "authorEmail" ) );
        Assert.assertEquals( "Copyright (c) 2011 John O'Connor", configJSON.getString( "copyright" ) );
        Assert.assertEquals( "", configJSON.getString( "contentType" ) );
        Assert.assertEquals( "utf-8", configJSON.getString( "contentCharset" ) );
        Assert.assertEquals( "Apache 2.0", configJSON.getString( "license" ) );
        Assert.assertEquals( "http://www.apache.org", configJSON.getString( "licenseURL" ) );
        Assert.assertEquals( "config.xml", configJSON.getString( "configXML" ) );
        Assert.assertEquals( "#000000", configJSON.getString( "loadingScreenColor" ) );
        Assert.assertEquals( "images/icon.png", configJSON.getString( "icon" ) );
        Assert.assertEquals( "images/icon.png", configJSON.getString( "iconHover" ) );
        Assert.assertEquals( "images/foregroundImg.jpg", configJSON.getString( "foregroundImage" ) );
        Assert.assertEquals( "images/backgroundImg.jpg", configJSON.getString( "backgroundImage" ) );
        Assert.assertEquals( "background.html", configJSON.getString( "backgroundSource" ) );
        Assert.assertEquals( "foreground.html", configJSON.getString( "foregroundSource" ) );           
        Assert.assertTrue( configJSON.getBoolean( "hasMultiAccess" ) );
        Assert.assertEquals( "default.html", configJSON.getString( "content" ) );
        Assert.assertTrue( configJSON.getBoolean( "debugEnabled" ) );
        Assert.assertTrue( configJSON.getBoolean( "allowInvokeParams" ) );
        Assert.assertTrue( configJSON.getBoolean( "runOnStartUp" ) );
        Assert.assertEquals( 300000, configJSON.getInt( "aggressiveCacheAge" ) );
        Assert.assertEquals( 1024, configJSON.getInt( "maxCacheSizeTotal" ) );
        Assert.assertEquals( 1024, configJSON.getInt( "maxCacheSizeItem" ) );
        
        JSONObject headers = configJSON.getJSONObject( "customHeaders" );
        Assert.assertNotNull( headers );        
        Set< String > headerKeys = headers.keySet();
        
        for( String key : headerKeys ) {
            if( key.equals( "webworks" ) ) {
                Assert.assertEquals( "rim/webworks", headers.getString( key ) );
            } else if( key.equals( "RIM-Widget" ) ) {
                Assert.assertEquals( "rim/widget", headers.getString( key ) );
            } else {
                Assert.fail( "customHeaders contains unknown header: " + key );
            }
        }
    }
    
    @Test
    public void testSerializeWhitelist() throws Exception {
        // mock SessionManager which is used by WidgetAccess for getting top-level domains
        final SessionManager session = _context.mock( SessionManager.class );
        _context.checking( new Expectations() {
            {
                allowing( session ).getTLD(); will( returnValue( TLD ) );
            }
        } );
        
        Class<?> c = SessionManager.class;
        Field singleton = c.getDeclaredField( "_instance" );
        singleton.setAccessible( true );
        singleton.set( null, session );
        
        // mock WidgetConfig, take values parsed from XML for granted        
        _widgetConfig = _context.mock( IWidgetConfig.class );
        _context.checking( new Expectations() {
            {
                allowing( _widgetConfig ).getName(); will( returnValue( null ) );
                allowing( _widgetConfig ).getVersion(); will( returnValue( null ) );
                allowing( _widgetConfig ).getID(); will( returnValue( null ) );
                allowing( _widgetConfig ).getDescription(); will( returnValue( null ) );
                allowing( _widgetConfig ).getContent(); will( returnValue( null ) );
                allowing( _widgetConfig ).getConfigXML(); will( returnValue( null ) );
                allowing( _widgetConfig ).getBackButtonBehaviour(); will( returnValue( null ) );
                allowing( _widgetConfig ).getContentType(); will( returnValue( null ) );
                allowing( _widgetConfig ).getContentCharSet(); will( returnValue( null ) );
                allowing( _widgetConfig ).getLicense(); will( returnValue( null ) );
                allowing( _widgetConfig ).getLicenseURL(); will( returnValue( null ) );
                allowing( _widgetConfig ).getAuthor(); will( returnValue( null ) );
                allowing( _widgetConfig ).getCopyright(); will( returnValue( null ) );
                allowing( _widgetConfig ).getAuthorEmail(); will( returnValue( null ) );
                allowing( _widgetConfig ).getLoadingScreenColour(); will( returnValue( null ) );
                allowing( _widgetConfig ).getBackgroundImage(); will( returnValue( null ) );
                allowing( _widgetConfig ).getForegroundImage(); will( returnValue( null ) );
                allowing( _widgetConfig ).getAuthorURL(); will( returnValue( null ) );
                allowing( _widgetConfig ).getBackgroundSource(); will( returnValue( null ) );
                allowing( _widgetConfig ).getForegroundSource(); will( returnValue( null ) );
                allowing( _widgetConfig ).allowMultiAccess(); will( returnValue( false ) );
                allowing( _widgetConfig ).getIconSrc(); will( returnValue( new Vector< String >() ) );
                allowing( _widgetConfig ).getCustomHeaders(); will( returnValue( new HashMap< String, String >() ) );
                allowing( _widgetConfig ).getNavigationMode(); will( returnValue( false ) );
                allowing( _widgetConfig ).getFirstPageLoad(); will( returnValue( false ) );
                allowing( _widgetConfig ).getLocalPageLoad(); will( returnValue( false ) );
                allowing( _widgetConfig ).getRemotePageLoad(); will( returnValue( false ) );
                allowing( _widgetConfig ).isCacheEnabled(); will( returnValue( null ) );
                allowing( _widgetConfig ).getAggressiveCacheAge(); will( returnValue( null ) );
                allowing( _widgetConfig ).getMaxCacheSize(); will( returnValue( null ) );
                allowing( _widgetConfig ).getMaxCacheItemSize(); will( returnValue( null ) );
                allowing( _widgetConfig ).isDebugEnabled(); will( returnValue( false ) );
                allowing( _widgetConfig ).allowInvokeParams(); will( returnValue( false ) );
                allowing( _widgetConfig ).isStartupEnabled(); will( returnValue( false ) );
                allowing( _widgetConfig ).getAccessTable(); will( returnValue( getTestAccessTable() ) );
            }
        } );        
        
        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();
        Assert.assertNotNull( configJSON );

        JSONArray accessList = configJSON.getJSONArray( "accessList" );
        Assert.assertNotNull( accessList );

        int accessListSize = accessList.size();
        Assert.assertEquals( 2, accessListSize );

        Iterator< JSONObject > it = accessList.iterator();
        while( it.hasNext() ) {
            JSONObject access = it.next();
            String uri = access.getString( "uri" );
            JSONArray features = access.getJSONArray( "features" );
            boolean allowSubDomain = access.getBoolean( "allowSubDomain" );
            Iterator< JSONObject > featureIt = features.iterator();

            Assert.assertNotNull( features );

            if( uri.equals( "WIDGET_LOCAL" ) ) {
                Assert.assertEquals( 2, features.size() );
                Assert.assertEquals( true, allowSubDomain );

                while( featureIt.hasNext() ) {
                    JSONObject feature = featureIt.next();
                    String id = feature.getString( "id" );

                    if( id.equals( "blackberry.system" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.app" ) ) {
                        assertFeature( feature, id, true, "1.0.0.0" );
                    } else {
                        Assert.fail( "feautres contains unknown feature: " + id );
                    }
                }
            } else if( uri.equals( "http://test-xp.rim.net" ) ) {
                Assert.assertEquals( 4, features.size() );
                Assert.assertEquals( false, allowSubDomain );

                while( featureIt.hasNext() ) {
                    JSONObject feature = featureIt.next();
                    String id = feature.getString( "id" );

                    if( id.equals( "blackberry.system" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.ui.dialog" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.io.file" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.media.microphone" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else {
                        Assert.fail( "feautres contains unknown feature: " + id );
                    }
                }
            } else {
                Assert.fail( "accessList contains unknown uri: " + uri );
            }
        }
    }
    
    private static void assertFeature( JSONObject feature, String id, boolean required, String version ) throws JSONException {
        Assert.assertEquals( required, feature.getBoolean( "required" ) );
        Assert.assertEquals( version, feature.getString( "version" ) );
        Assert.assertEquals( id, feature.getString( "id" ) );
    }

    @After
    public void tearDown() throws Exception {
        _widgetConfig = null;
        _serializer = null;
    }
}
