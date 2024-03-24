package org.openelisglobal.organizationContact;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;



import org.openelisglobal.organization.valueholder.Organization;
import org.openelisglobal.organization.valueholder.OrganizationContact;
import  org.openelisglobal.person.valueholder.Person;




public class OrganizationContactIntegrationTest {
    private Person person;
    private Organization organization;

    @Before
    public void setUp() {
        // Initialize Person
        person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");

        // Initialize Organization
        organization = new Organization();
        organization.setId("123");
    }

    @Test
    public void testOrganizationContactIntegration() {
        // Create an instance of OrganizationContact and associate it with both Person and Organization
        OrganizationContact organizationContact = new OrganizationContact();
        organizationContact.setPerson(person);
        organizationContact.setOrganizationId(organization.getId());

        // Validate that OrganizationContact is associated with the correct Person and Organization
        assertEquals("John", organizationContact.getPerson().getFirstName());
        assertEquals("Doe", organizationContact.getPerson().getLastName());
        assertEquals(person, organizationContact.getPerson());
        assertEquals("123", organizationContact.getOrganizationId());
        
    }
}
