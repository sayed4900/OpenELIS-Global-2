package org.openelisglobal.common.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.openelisglobal.common.constants.Constants;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.services.DisplayListService.ListType;
import org.openelisglobal.common.services.IStatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.util.ConfigurationProperties.Property;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.person.service.PersonService;
import org.openelisglobal.person.valueholder.Person;
import org.openelisglobal.provider.service.ProviderService;
import org.openelisglobal.provider.valueholder.Provider;
import org.openelisglobal.role.service.RoleService;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.systemuser.service.UserService;
import org.openelisglobal.test.service.TestService;
import org.openelisglobal.test.service.TestServiceImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.typeofsample.service.TypeOfSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/")
public class DisplayListController extends BaseRestController{

    @Autowired
    private ProviderService  providerService;

    @Autowired
    private PersonService personService;
    
	@Autowired
	private UserService userService;
	
	@Autowired
	protected TestService testService;
	
	@Autowired
	private RoleService roleService;

	@Autowired
    TypeOfSampleService typeOfSampleService;
	
	private static boolean HAS_NFS_PANEL = false;

	static {
		HAS_NFS_PANEL = ConfigurationProperties.getInstance().isPropertyValueEqual(Property.CONDENSE_NFS_PANEL, "true");
	}
	

	protected static List<Integer> statusList;
	protected static List<String> nfsTestIdList;

	@PostConstruct
	private void initialize() {
		if (statusList == null) {
			statusList = new ArrayList<>();
			statusList.add(Integer
					.parseInt(SpringContext.getBean(IStatusService.class).getStatusID(AnalysisStatus.NotStarted)));
			statusList.add(Integer.parseInt(
					SpringContext.getBean(IStatusService.class).getStatusID(AnalysisStatus.BiologistRejected)));
			statusList.add(Integer.parseInt(
					SpringContext.getBean(IStatusService.class).getStatusID(AnalysisStatus.TechnicalRejected)));
			statusList.add(Integer.parseInt(
					SpringContext.getBean(IStatusService.class).getStatusID(AnalysisStatus.NonConforming_depricated)));
		}

		if (nfsTestIdList == null) {
			nfsTestIdList = new ArrayList<>();
			nfsTestIdList.add(getTestId("GB"));
			nfsTestIdList.add(getTestId("Neut %"));
			nfsTestIdList.add(getTestId("Lymph %"));
			nfsTestIdList.add(getTestId("Mono %"));
			nfsTestIdList.add(getTestId("Eo %"));
			nfsTestIdList.add(getTestId("Baso %"));
			nfsTestIdList.add(getTestId("GR"));
			nfsTestIdList.add(getTestId("Hb"));
			nfsTestIdList.add(getTestId("HCT"));
			nfsTestIdList.add(getTestId("VGM"));
			nfsTestIdList.add(getTestId("TCMH"));
			nfsTestIdList.add(getTestId("CCMH"));
			nfsTestIdList.add(getTestId("PLQ"));
		}

	}
	
	protected String getTestId(String testName) {
		Test test = testService.getTestByLocalizedName(testName);
		if (test == null) {
			test = new Test();
		}
		return test.getId();

	}

	protected List<IdValuePair> adjustNFSTests(List<IdValuePair> allTestsList) {
		List<IdValuePair> adjustedList = new ArrayList<>(allTestsList.size());
		for (IdValuePair idValuePair : allTestsList) {
			if (!nfsTestIdList.contains(idValuePair.getId())) {
				adjustedList.add(idValuePair);
			}
		}
		// add NFS to the list
		adjustedList.add(new IdValuePair("NFS", "NFS"));
		return adjustedList;
	}

	protected boolean allNFSTestsRequested(List<String> testIdList) {
		return (testIdList.containsAll(nfsTestIdList));

	}
	

    @GetMapping(value = "displayList/{listType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getDisplayList(@PathVariable DisplayListService.ListType listType) {
        return DisplayListService.getInstance().getList(listType);
    }

    @GetMapping(value = "tests", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getTests() {
        return DisplayListService.getInstance().getList(ListType.ALL_TESTS);
    }

	@GetMapping(value = "tests-by-sample", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getTestsBySample( @RequestParam String sampleType) {
		 List<IdValuePair> tests = new ArrayList<>();
		 List<Test> testList = new ArrayList<>();
        if (StringUtils.isNotBlank(sampleType)) {
            testList = typeOfSampleService.getActiveTestsBySampleTypeId(sampleType, false);
        } else {
            return tests;
        }
		
        testList.forEach(test -> { tests.add(new IdValuePair(test.getId(), TestServiceImpl.getLocalizedTestNameWithType(test)));});
        return tests;
    }

    @GetMapping(value = "samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getSamples() {
        return DisplayListService.getInstance().getList(ListType.SAMPLE_TYPE_ACTIVE);
    }

    @GetMapping(value = "health-regions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getHealthRegions() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_HEALTH_REGIONS);
    }

    @GetMapping(value = "education-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getEducationList() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_EDUCATION);
    }

    @GetMapping(value = "marital-statuses", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getMaritialList() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_MARITAL_STATUS);
    }

    @GetMapping(value = "nationalities", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getNationalityList() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_NATIONALITY);
    }

    @GetMapping(value = "programs", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getPrograms() {
        return DisplayListService.getInstance().getList(ListType.PROGRAM);
    }

    @GetMapping(value = "dictionaryPrograms", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getDictionaryPrograms() {
        return DisplayListService.getInstance().getList(ListType.DICTIONARY_PROGRAM);
    }

    @GetMapping(value = "patientPaymentsOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getSamplePatientPaymentOptions() {
        return DisplayListService.getInstance().getList(ListType.SAMPLE_PATIENT_PAYMENT_OPTIONS);
    }

    @GetMapping(value = "testLocationCodes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getTestLocationCodes() {
        return DisplayListService.getInstance().getList(ListType.TEST_LOCATION_CODE);
    }

    @GetMapping(value = "test-rejection-reasons", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<IdValuePair> getTestRejectionReasons() {
        return DisplayListService.getInstance().getList(ListType.REJECTION_REASONS);
    }

    @GetMapping(value = "referral-reasons", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private List<IdValuePair> createReferralReasonList() {
        return DisplayListService.getInstance().getList(ListType.REFERRAL_REASONS);
    }

    @GetMapping(value = "referral-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private List<IdValuePair> createReferralOrganizationsList() {
        return DisplayListService.getInstance().getList(ListType.REFERRAL_ORGANIZATIONS);
    }

    @GetMapping(value = "site-names", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private List<IdValuePair> getSiteNameList() {
        return DisplayListService.getInstance().getList(ListType.SAMPLE_PATIENT_REFERRING_CLINIC);
    }


    @GetMapping(value = "configuration-properties", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private List<IdValuePair> getConfigurationProperties() {
        ConfigurationProperties.forceReload();

        List<IdValuePair> configs = new ArrayList<>();
	    configs.add(new IdValuePair("restrictFreeTextProviderEntry", ConfigurationProperties.getInstance().getPropertyValue(
			    ConfigurationProperties.Property.restrictFreeTextProviderEntry)));
	    configs.add(new IdValuePair("restrictFreeTextRefSiteEntry", ConfigurationProperties.getInstance().getPropertyValue(
			    ConfigurationProperties.Property.restrictFreeTextRefSiteEntry)));
	    configs.add(new IdValuePair("currentDateAsText", DateUtil.getCurrentDateAsText()));
	    configs.add(new IdValuePair("currentTimeAsText", DateUtil.getCurrentTimeAsText()));
        return configs;
	}

    @GetMapping(value = "practitioner", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private Provider getProviderInformation(@RequestParam String providerId) {
        if (providerId != null){
            Person person = personService.getPersonById(providerId);
            Provider provider = providerService.getProviderByPerson(person);
            return provider;
        }
        return null;
    }

	@GetMapping(value = "test-list", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	private List<IdValuePair> getTestDropdownList(HttpServletRequest request) {
		List<IdValuePair> testList = userService.getAllDisplayUserTestsByLabUnit(getSysUserId(request),
				Constants.ROLE_RESULTS);

		if (HAS_NFS_PANEL) {
			testList = adjustNFSTests(testList);
		}
		Collections.sort(testList, new ValueComparator());
		return testList;
	}
	
	@GetMapping(value = "priorities", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	private List<IdValuePair> createPriorityList() {
		return DisplayListService.getInstance().getList(ListType.ORDER_PRIORITY);
	}
	
	@GetMapping(value = "panels", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	private List<IdValuePair> createPanelList() {
		return DisplayListService.getInstance().getList(ListType.PANELS);
	}
	
	@GetMapping(value = "test-sections", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	private List<IdValuePair> createTestSectionsList() {
		return DisplayListService.getInstance().getList(ListType.REFERRAL_ORGANIZATIONS);
	}

	@GetMapping(value = "user-test-sections", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	private List<IdValuePair> createUserTestSectionsList(HttpServletRequest request) {
		String resultsRoleId = roleService.getRoleByName(Constants.ROLE_RESULTS).getId();
		return userService.getUserTestSections(getSysUserId(request), resultsRoleId);
	}
	
	
	
	
	
	class ValueComparator implements Comparator<IdValuePair> {

		@Override
		public int compare(IdValuePair p1, IdValuePair p2) {
			return p1.getValue().toUpperCase().compareTo(p2.getValue().toUpperCase());
		}
	}
	
}
