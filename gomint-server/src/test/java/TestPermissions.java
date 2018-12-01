/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

import io.gomint.permission.Group;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.permission.PermissionGroupManager;
import io.gomint.server.permission.PermissionManager;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author geNAZt
 * @version 1.0
 */
public class TestPermissions {

    private EntityPlayer mockPlayer( boolean op ) {
        final EntityPlayer player = mock( EntityPlayer.class );
        when( player.isOp() ).thenReturn( op );
        return player;
    }

    @Test
    public void testNormal() {
        PermissionManager permissionManager = new PermissionManager( mockPlayer( false ) );
        permissionManager.setPermission( "test.permission.shouldbetrue", true );
        permissionManager.setPermission( "test.permission.shouldfalseforreal", false );

        Assert.assertTrue( "Wildcard is false", permissionManager.hasPermission( "test.permission.shouldbetrue" ) );
        Assert.assertFalse( "Wildcard is true", permissionManager.hasPermission( "test.permission.shouldfalseforreal" ) );
    }

    @Test
    public void testWildcard() {
        PermissionManager permissionManager = new PermissionManager( mockPlayer( false ) );
        permissionManager.setPermission( "test.permission.*", true );
        permissionManager.setPermission( "test.permission.shouldfalse*", false );
        permissionManager.setPermission( "test.permission.shouldfalseforrealno", true );

        Assert.assertTrue( "Wildcard is false", permissionManager.hasPermission( "test.permission.shouldbetrue" ) );
        Assert.assertFalse( "Wildcard is true", permissionManager.hasPermission( "test.permission.shouldfalseforreal" ) );
        Assert.assertTrue( "Wildcard is false", permissionManager.hasPermission( "test.permission.shouldfalseforrealno" ) );
    }

    @Test
    public void testOneGroup() {
        PermissionGroupManager permissionGroupManager = new PermissionGroupManager();
        Group groupA = permissionGroupManager.getOrCreateGroup( "A" );
        groupA.setPermission( "test.permission.*", true );
        groupA.setPermission( "test.permission.shouldfalse*", false );
        groupA.setPermission( "test.permission.shouldfalseforrealno", true );

        PermissionManager permissionManager = new PermissionManager( mockPlayer( false ) );
        permissionManager.addGroup( groupA );

        Assert.assertTrue( "Wildcard is false", permissionManager.hasPermission( "test.permission.shouldbetrue" ) );
        Assert.assertFalse( "Wildcard is true", permissionManager.hasPermission( "test.permission.shouldfalseforreal" ) );
        Assert.assertTrue( "Wildcard is false", permissionManager.hasPermission( "test.permission.shouldfalseforrealno" ) );
    }

    @Test
    public void testGroupOverride() {
        PermissionGroupManager permissionGroupManager = new PermissionGroupManager();

        Group groupA = permissionGroupManager.getOrCreateGroup( "A" );
        groupA.setPermission( "test.permission.*", false );
        groupA.setPermission( "test.permission.shouldfalse*", true );
        groupA.setPermission( "test.permission.shouldfalseforrealno", false );

        Group groupB = permissionGroupManager.getOrCreateGroup( "B" );
        groupB.setPermission( "test.permission.*", true );
        groupB.setPermission( "test.permission.shouldfalse*", false );
        groupB.setPermission( "test.permission.shouldfalseforrealno", true );

        PermissionManager permissionManager = new PermissionManager( mockPlayer( false ) );
        permissionManager.addGroup( groupA );
        permissionManager.addGroup( groupB );

        Assert.assertTrue( "Wildcard is false", permissionManager.hasPermission( "test.permission.shouldbetrue" ) );
        Assert.assertFalse( "Wildcard is true", permissionManager.hasPermission( "test.permission.shouldfalseforreal" ) );
        Assert.assertTrue( "Wildcard is false", permissionManager.hasPermission( "test.permission.shouldfalseforrealno" ) );
    }

    @Test
    public void testOp() {
        final PermissionManager permissionManager = new PermissionManager( mockPlayer( true ) );
        permissionManager.setPermission( "test.permission.*", false );
        permissionManager.setPermission( "test.permission.false*", false );
        permissionManager.setPermission( "test.permission.true", true );
        permissionManager.setPermission( "test.permission.falseforreal", false );
        permissionManager.setPermission( "test.permission.alsofalse", false );

        Assert.assertTrue( permissionManager.hasPermission( "test.otherpermission" ) );
        Assert.assertTrue( permissionManager.hasPermission( "test.permission" ) );
        Assert.assertTrue( permissionManager.hasPermission( "test.permission.someotherperm" ) );
        Assert.assertTrue( permissionManager.hasPermission( "test.permission.shouldfalseforreal" ) );
        Assert.assertTrue( permissionManager.hasPermission( "test.permission.true" ) );
        Assert.assertTrue( permissionManager.hasPermission( "test.permission.falseforreal" ) );
        Assert.assertTrue( permissionManager.hasPermission( "test.permission.alsofalse" ) );
        Assert.assertTrue( permissionManager.hasPermission( "test.permission.false123" ) );
    }
}
