package org.ldscd.callingworkflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.model.permissions.Authorizable;
import org.ldscd.callingworkflow.model.permissions.AuthorizableOrg;
import org.ldscd.callingworkflow.model.permissions.AuthorizableUnit;
import org.ldscd.callingworkflow.model.permissions.OrgPermissionResolver;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.model.permissions.PermissionResolver;
import org.ldscd.callingworkflow.model.permissions.UnitPermissionResolver;
import org.ldscd.callingworkflow.model.permissions.UnitRole;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.model.permissions.constants.PositionType;
import org.ldscd.callingworkflow.model.permissions.constants.Role;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class PermissionsTests {

    static final Long mainUnit = 1234L;

    PermissionManager permissionManager;
    @Mock
    PermissionResolver permissionResolver;

    @Before
    public void startUp() {
        permissionManager = new PermissionManager();
    }
    @Test
    public void testRolePermissions() {
        /* Unit Admin Test */
        assertEquals(Role.UNIT_ADMIN.getPermissions().size(), 11);
        List<Permission> permissions = Role.UNIT_ADMIN.getPermissions();
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_DELETE));
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_UPDATE));
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_RELEASE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_DELETE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_UPDATE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_CREATE));
        assertTrue(permissions.contains(Permission.ORG_INFO_READ));
        assertTrue(permissions.contains(Permission.PRIESTHOOD_OFFICE_READ));
        assertTrue(permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_CREATE));
        assertTrue(permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_UPDATE));

        /* Priesthood Admin Test */
        permissions = Role.PRIESTHOOD_ORG_ADMIN.getPermissions();
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_DELETE));
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_UPDATE));
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_RELEASE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_DELETE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_UPDATE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_CREATE));
        assertTrue(permissions.contains(Permission.ORG_INFO_READ));
        assertTrue(permissions.contains(Permission.PRIESTHOOD_OFFICE_READ));
        assertTrue(!permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_CREATE));
        assertTrue(!permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_UPDATE));

        /* Org Admin Test */
        permissions = Role.ORG_ADMIN.getPermissions();
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_DELETE));
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_UPDATE));
        assertTrue(permissions.contains(Permission.ACTIVE_CALLING_RELEASE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_DELETE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_UPDATE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_CREATE));
        assertTrue(permissions.contains(Permission.ORG_INFO_READ));
        assertTrue(!permissions.contains(Permission.PRIESTHOOD_OFFICE_READ));
        assertTrue(!permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_CREATE));
        assertTrue(!permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_UPDATE));

        /* Stake Assistant Test */
        permissions = Role.STAKE_ASSISTANT.getPermissions();
        assertTrue(!permissions.contains(Permission.ACTIVE_CALLING_DELETE));
        assertTrue(!permissions.contains(Permission.ACTIVE_CALLING_UPDATE));
        assertTrue(!permissions.contains(Permission.ACTIVE_CALLING_RELEASE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_DELETE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_UPDATE));
        assertTrue(permissions.contains(Permission.POTENTIAL_CALLING_CREATE));
        assertTrue(permissions.contains(Permission.ORG_INFO_READ));
        assertTrue(!permissions.contains(Permission.PRIESTHOOD_OFFICE_READ));
        assertTrue(!permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_CREATE));
        assertTrue(!permissions.contains(Permission.UNIT_GOOGLE_ACCOUNT_UPDATE));
    }

    @Test
    public void testPositionTypes() {
        /* Unit Admins */
        List<Integer> bishopricPosIds = new ArrayList<>( Arrays.asList(4, 54, 55, 56, 57));
        List<Integer> branchPresPosIds = new ArrayList<>( Arrays.asList(12, 59, 60, 789, 1278));
        List<Integer> stakePosIds = new ArrayList<>( Arrays.asList(1, 2, 3, 94, 51, 52));
        List<Integer> unitAdminPositionIds = new ArrayList<>();
            unitAdminPositionIds.addAll(bishopricPosIds);
            unitAdminPositionIds.addAll(branchPresPosIds);
            unitAdminPositionIds.addAll(stakePosIds);
        /* Priesthood Admins */
        List<Integer> hpPosIds = new ArrayList<>( Arrays.asList(133, 134, 135, 136));
        List<Integer> eqPosIds = new ArrayList<>( Arrays.asList(138, 139, 140, 141));
        List<Integer> rsPosIds = new ArrayList<>( Arrays.asList(143, 144, 145, 146));
        List<Integer> ymPosIds = new ArrayList<>( Arrays.asList(158, 159, 160, 161));
        List<Integer> ywPosIds = new ArrayList<>( Arrays.asList(183, 184, 185, 186));
        List<Integer> ssPosIds = new ArrayList<>( Arrays.asList(204, 205, 206, 207));
        List<Integer> priPosIds = new ArrayList<>( Arrays.asList(210, 211, 212, 213));
        List<Integer> priesthoodAdminPositionIds = new ArrayList<>();
            priesthoodAdminPositionIds.addAll(hpPosIds);
            priesthoodAdminPositionIds.addAll(eqPosIds);
            priesthoodAdminPositionIds.addAll(ymPosIds);
        /* Org Admins */
        List<Integer> orgAdminPositionIds = new ArrayList<>();
            orgAdminPositionIds.addAll(rsPosIds);
            orgAdminPositionIds.addAll(ywPosIds);
            orgAdminPositionIds.addAll(ssPosIds);
            orgAdminPositionIds.addAll(priPosIds);
        /* All valid position ID's */
        List<Integer> validPositionIds = new ArrayList<>();
            validPositionIds.addAll(unitAdminPositionIds);
            validPositionIds.addAll(orgAdminPositionIds);
            validPositionIds.addAll(priesthoodAdminPositionIds);

        /* Validate all position types are available */
        for(Integer positionId : validPositionIds) {
            assertNotNull(PositionType.get(positionId));
        }

        /* Validate we do not give rights where they are not allowed */
        for(int i = 0; i < 1000; i++) {
            if(!validPositionIds.contains(i)) {
                assertNull(PositionType.get(i));
            }
        }

        /* Validate all UnitRoles being created for a specified position are the correct roles and in the proper orgType */
        testUnitRoleCreatedForPosition(hpPosIds, UnitLevelOrgType.HIGH_PRIESTS, Role.PRIESTHOOD_ORG_ADMIN);
        testUnitRoleCreatedForPosition(eqPosIds, UnitLevelOrgType.ELDERS, Role.PRIESTHOOD_ORG_ADMIN);
        testUnitRoleCreatedForPosition(rsPosIds, UnitLevelOrgType.RELIEF_SOCIETY, Role.ORG_ADMIN);
        testUnitRoleCreatedForPosition(ymPosIds, UnitLevelOrgType.YOUNG_MEN, Role.PRIESTHOOD_ORG_ADMIN);
        testUnitRoleCreatedForPosition(ywPosIds, UnitLevelOrgType.YOUNG_WOMEN, Role.ORG_ADMIN);
        testUnitRoleCreatedForPosition(ssPosIds, UnitLevelOrgType.SUNDAY_SCHOOL, Role.ORG_ADMIN);
        testUnitRoleCreatedForPosition(priPosIds, UnitLevelOrgType.PRIMARY, Role.ORG_ADMIN);
        testUnitRoleCreatedForPosition(bishopricPosIds, UnitLevelOrgType.BISHOPRIC, Role.UNIT_ADMIN);
        testUnitRoleCreatedForPosition(branchPresPosIds, UnitLevelOrgType.BRANCH_PRESIDENCY, Role.UNIT_ADMIN);
        testUnitRoleCreatedForPosition(stakePosIds, UnitLevelOrgType.STAKE_PRESIDENCY, Role.UNIT_ADMIN);
    }

    private void testUnitRoleCreatedForPosition(List<Integer> positionIds, UnitLevelOrgType orgType, Role role) {
        List<UnitRole> unitRoles = permissionManager.createUnitRoles(createPositions(positionIds, mainUnit), mainUnit);
        assertEquals(unitRoles.get(0).getRole(), role);
        assertEquals(unitRoles.get(0).getOrgType(), orgType);
    }

    @Test
    public void testUnitPermissionResolver() {
        permissionResolver = new UnitPermissionResolver();
        Position positionDontCare = new Position("Don't Care", 100, false, false, 1100L, mainUnit);
        AuthorizableUnit authorizableUnit = new AuthorizableUnit(mainUnit);
        AuthorizableOrg authorizableOrg = new AuthorizableOrg(mainUnit, UnitLevelOrgType.ELDERS, 66);
        UnitRole authorizedUnitRole = new UnitRole(Role.UNIT_ADMIN, mainUnit, null, UnitLevelOrgType.BISHOPRIC, positionDontCare);
        UnitRole nonAuthorizedUnitRole = new UnitRole(Role.UNIT_ADMIN, 2748L, null, UnitLevelOrgType.SUNDAY_SCHOOL, positionDontCare);

        testPermissionResolver(permissionResolver, authorizedUnitRole, authorizableUnit, true);
        testPermissionResolver(permissionResolver, authorizedUnitRole, authorizableOrg, true);
        testPermissionResolver(permissionResolver, authorizedUnitRole, authorizableUnit, true);
        testPermissionResolver(permissionResolver, nonAuthorizedUnitRole, authorizableUnit, false);
        testPermissionResolver(permissionResolver, nonAuthorizedUnitRole, authorizableUnit, false);
    }

    @Test
    public void testOrgPermissionResolver() {
        permissionResolver = new OrgPermissionResolver();
        Position positionDontCare = new Position("some position", 0, false, false, 100000l, mainUnit);
        AuthorizableOrg eldersQuorumOrg = new AuthorizableOrg(mainUnit, UnitLevelOrgType.ELDERS, 66);
        AuthorizableOrg eldersQuorumPresidencyOrg = new AuthorizableOrg(mainUnit, UnitLevelOrgType.ELDERS, UnitLevelOrgType.ELDERS.getExceptionId());

        UnitRole eldersQuorumAdmin = new UnitRole(Role.PRIESTHOOD_ORG_ADMIN, mainUnit, null, UnitLevelOrgType.ELDERS, positionDontCare);
        UnitRole eldersQuorumAdminOtherUnit = new UnitRole(Role.PRIESTHOOD_ORG_ADMIN, mainUnit + 100, null, UnitLevelOrgType.SUNDAY_SCHOOL, positionDontCare);

        UnitRole reliefSocietyAdmin = new UnitRole(Role.ORG_ADMIN, mainUnit, null, UnitLevelOrgType.RELIEF_SOCIETY, positionDontCare);

        testPermissionResolver(permissionResolver, eldersQuorumAdmin, eldersQuorumOrg, true);
        testPermissionResolver(permissionResolver, reliefSocietyAdmin, eldersQuorumOrg, false);
        testPermissionResolver(permissionResolver, eldersQuorumAdminOtherUnit, eldersQuorumOrg, false);
        testPermissionResolver(permissionResolver, eldersQuorumAdmin, eldersQuorumPresidencyOrg, false);
    }
    private void testPermissionResolver(PermissionResolver permissionResolver, UnitRole unitRole, Authorizable authorizable, boolean expectedResult) {
        assertEquals(permissionResolver.isAuthorized(unitRole, authorizable), expectedResult);
    }

    @Test
    public void testCreateUnitRoles() {
        List<Position> noRoles = createPositions(Arrays.asList(200, 100), mainUnit);
        Position unitAdmin = createPosition(null, 4, false, true, 1000L, mainUnit);
        Position otherUnitAdmin = createPosition(null, 4, false, true, 1000L, mainUnit + 111);
        Position eldersQuorumPresident = createPosition(null, 138, false, true, 1000L, mainUnit);
        Position sundaySchoolPresident = createPosition(null, 205, false, true, 1000L, mainUnit);

        UnitRole unitAdminRole = new UnitRole(Role.UNIT_ADMIN, mainUnit, null, UnitLevelOrgType.BISHOPRIC, null);
        UnitRole eldersQuorumAdminRole = new UnitRole(Role.PRIESTHOOD_ORG_ADMIN, mainUnit, null, UnitLevelOrgType.ELDERS, null);
        UnitRole sundaySchoolAdminRole = new UnitRole(Role.ORG_ADMIN, mainUnit, null, UnitLevelOrgType.SUNDAY_SCHOOL, null);

        /* These should not have any any roles created. */
        List<Position> noRolesTestCases = new ArrayList<>(noRoles);
        noRolesTestCases.add(otherUnitAdmin);
        assertTrue(permissionManager.createUnitRoles(noRolesTestCases, mainUnit).isEmpty());

        List<Position> singleAdminTestCases = new ArrayList<>();
        /* All these positions should result in a single unit admin role for the unit */
        testUnitAdminRoles(Collections.singletonList(unitAdmin), mainUnit, unitAdminRole, 1);
        singleAdminTestCases.addAll(noRoles);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, unitAdminRole, 1);
        singleAdminTestCases.clear();
        singleAdminTestCases.add(eldersQuorumPresident);
        singleAdminTestCases.add(unitAdmin);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, unitAdminRole, 1);
        singleAdminTestCases.add(otherUnitAdmin);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, unitAdminRole, 1);
        singleAdminTestCases.clear();
        singleAdminTestCases.add(eldersQuorumPresident);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, eldersQuorumAdminRole, 1);
        singleAdminTestCases.clear();
        singleAdminTestCases.addAll(noRoles);
        singleAdminTestCases.add(eldersQuorumPresident);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, eldersQuorumAdminRole, 1);
        singleAdminTestCases.add(otherUnitAdmin);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, eldersQuorumAdminRole, 1);
        singleAdminTestCases.clear();

        /* All these positions should result in multiple unit admin role for the unit */
        singleAdminTestCases.add(eldersQuorumPresident);
        singleAdminTestCases.add(sundaySchoolPresident);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, sundaySchoolAdminRole, 2);
        singleAdminTestCases.addAll(noRoles);
        testUnitAdminRoles(singleAdminTestCases, mainUnit, sundaySchoolAdminRole, 2);
    }
    private void testUnitAdminRoles(List<Position> positions, Long unitNumber, UnitRole expectedUnitRole, int size) {
        List<UnitRole> unitRoles = permissionManager.createUnitRoles(positions, unitNumber);
        if(size == 1) {
            assertTrue(unitRoles.size() == size);
        } else {
            assertTrue(unitRoles.size() > 1);
        }

        assertEquals(unitRoles.get(0).getOrgType(), expectedUnitRole.getOrgType());
        assertEquals(unitRoles.get(0).getOrgId(), expectedUnitRole.getOrgId());
        assertEquals(unitRoles.get(0).getUnitNum(), expectedUnitRole.getUnitNum());
        assertEquals(unitRoles.get(0).getOrgRightsException(), expectedUnitRole.getOrgRightsException());
        assertEquals(unitRoles.get(0).getRole(), expectedUnitRole.getRole());
    }

    @Test
    public void testHasPermission(){
        Position unitAdmin = createPosition("Unit Admin", 4, false, false, 1000L, mainUnit);
        Position priesthoodOrgAdmin = createPosition("Priesthood Org Admin", 138, false, false, 1000L, mainUnit);
        Position orgAdmin = createPosition("Org Admin", 204, false, false, 1000L, mainUnit);

        UnitRole unitAdminRole = new UnitRole(Role.UNIT_ADMIN, mainUnit, null, null, unitAdmin);
        UnitRole priesthoodOrgAdminRole = new UnitRole(Role.PRIESTHOOD_ORG_ADMIN, mainUnit, null, null, priesthoodOrgAdmin);
        UnitRole orgAdminRole = new UnitRole(Role.ORG_ADMIN, mainUnit, null, null, orgAdmin);

        testHasPermissions(unitAdminRole, Permission.ORG_INFO_READ, true);
        testHasPermissions(orgAdminRole, Permission.POTENTIAL_CALLING_UPDATE, true);
        testHasPermissions(priesthoodOrgAdminRole, Permission.POTENTIAL_CALLING_DELETE, true);
        testHasPermissions(priesthoodOrgAdminRole, Permission.PRIESTHOOD_OFFICE_READ, true);
        testHasPermissions(unitAdminRole, Permission.UNIT_GOOGLE_ACCOUNT_UPDATE, true);
        testHasPermissions(orgAdminRole, Permission.UNIT_GOOGLE_ACCOUNT_UPDATE, false);
        testHasPermissions(orgAdminRole, Permission.PRIESTHOOD_OFFICE_READ, false);
        testHasPermissions(unitAdminRole, Permission.ORG_INFO_UPDATE, false);
        testHasPermissions(unitAdminRole, Permission.POTENTIAL_CALLING_UPDATE, true);
        testHasPermissions(orgAdminRole, Permission.UNIT_GOOGLE_ACCOUNT_UPDATE, false);
    }
    private void testHasPermissions(UnitRole unitRole, Permission permission, boolean expectedResult) {
        assertEquals(permissionManager.hasPermission(unitRole, permission), expectedResult);
    }

    private List<Position> createPositions(List<Integer> positionIds, Long unitNumber) {
        List<Position> positions = new ArrayList<>(positionIds.size());
        for(Integer positionId : positionIds) {
            positions.add(createPosition("SomeName", positionId, false, true, 100L, unitNumber));
        }
        return positions;
    }

    private Position createPosition(String name, Integer positionId, boolean hidden, boolean allowMultiples, long displayOrder, long unitNumber) {
        return new Position(name, positionId, hidden, allowMultiples, displayOrder, unitNumber);
    }
}