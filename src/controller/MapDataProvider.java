package controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import rdf.RDFModel;
import ui.map.AbstractLAKMap;
import util.StringCouple;
import util.StringUtil;
import util.location.CountryLocationCache;
import util.location.OrganizationCountryMap;
import util.location.OrganizationLocationCache;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.fhpotsdam.unfolding.geo.Location;

public class MapDataProvider extends DataProvider implements AbstractLAKMap.DataProvider {

	private final CountryLocationCache countryLocationCache;
	private final OrganizationLocationCache organizationLocationCache;
	private final OrganizationCountryMap organizationCountryMap;

	private final SortedSet<String> allOrganizations;
	private final Map<StringCouple, Integer> allOrganizationCooperation;
	private final Map<String, Map<StringCouple, Integer>> organizationCooperationForConferenceAcronyms;

	public MapDataProvider(String organizationLocationFile, String countryLocationFile, String organizationCountryFile)
			throws IOException {
		countryLocationCache = new CountryLocationCache(countryLocationFile);
		organizationLocationCache = new OrganizationLocationCache(organizationLocationFile);
		organizationCountryMap = new OrganizationCountryMap(organizationCountryFile);
		
		allOrganizations = new TreeSet<>();
		allOrganizationCooperation = new HashMap<>();
		organizationCooperationForConferenceAcronyms = new HashMap<>();
	}

	@Override
	public Location getCountryLocation(String country) {
		return countryLocationCache.get(country);
	}

	@Override
	public Location getOrganizationLocation(String organization) {
		return organizationLocationCache.get(organization);
	}

	@Override
	public String getCountry(String organization) {
		return organizationCountryMap.get(organization);
	}

	@Override
	public SortedSet<String> getAllOrganizations() {
		if (allOrganizations.isEmpty()) {
			synchronized(allOrganizations) {
				if (!allOrganizations.isEmpty())
					return allOrganizations;
				
				ResultSet data = RDFModel.getAllOrganisations();
				Set<String> newAllOrganizations = new HashSet<>();
				
				while (data.hasNext()) {
					newAllOrganizations.add(StringUtil.getString(data.next().getLiteral("orgname")));
				}
				
				allOrganizations.addAll(newAllOrganizations);
			}
		}

		return allOrganizations;
	}

	@Override
	public Set<String> getOrganizationsForConference(String conferenceAcronym) {
		return getOrganizationsForConference(conferenceAcronym, null);
	}

	@Override
	public Set<String> getOrganizationsForConference(String conferenceAcronym, Set<String> organizations) {
		if (organizations == null) {
			organizations = new HashSet<>();
		}
		
		ConfYearCouple conference = new ConfYearCouple(conferenceAcronym);
		organizations.addAll(getOrganizationsForConference(conference.conference, conference.year));
		
		return organizations;
	}

	@Override
	public Set<StringCouple> getOrganizationCooperationsForConference(String conferenceAcronym) {
		return getOrganizationCooperationsForConference(conferenceAcronym, null);
	}

	@Override
	public Set<StringCouple> getOrganizationCooperationsForConference(String conferenceAcronym,
			Set<StringCouple> organizationCooperation) {
		if (organizationCooperation == null) {
			organizationCooperation = new HashSet<>();
		}
		
		if (!organizationCooperationForConferenceAcronyms.containsKey(conferenceAcronym)) {
			synchronized (organizationCooperationForConferenceAcronyms) {
				loadConferenceOrganizationCooperationData(conferenceAcronym);
			}
		}
		
		organizationCooperation.addAll(organizationCooperationForConferenceAcronyms.get(conferenceAcronym).keySet());
		
		return organizationCooperation;
	}

	@Override
	public Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym) {
		return getOrganizationCooperationDataForConference(conferenceAcronym, null);
	}

	@Override
	public Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym,
			Map<StringCouple, Integer> data) {
		if (!organizationCooperationForConferenceAcronyms.containsKey(conferenceAcronym)) {
			synchronized (organizationCooperationForConferenceAcronyms) {
				loadConferenceOrganizationCooperationData(conferenceAcronym);
			}
		}
		
		final Map<StringCouple, Integer> conferenceData = organizationCooperationForConferenceAcronyms.get(conferenceAcronym);
		
		if (data == null) {
			return new HashMap<>(conferenceData);
		}
		
		for (StringCouple conferenceCouple : conferenceData.keySet()) {
			if (data.containsKey(conferenceCouple)) {
				data.put(conferenceCouple, data.get(conferenceCouple) + conferenceData.get(conferenceCouple));
			} else {
				data.put(conferenceCouple, conferenceData.get(conferenceCouple));
			}
		}
		
		return data;
	}

	@Override
	public Map<StringCouple, Integer> getAllOrganizationCooperationData() {
		if (allOrganizationCooperation.isEmpty()) {
			synchronized (allOrganizationCooperation) {
				if (!allOrganizationCooperation.isEmpty())
					return allOrganizationCooperation;
				
				Map<StringCouple, Integer> newAllOrganizationCooperations = new HashMap<>();
				
				ResultSet rs = RDFModel.getAllOrganisationPairsThatWroteAPaperTogether();
				
				QuerySolution sol;
				while (rs.hasNext()) {
					sol = rs.next();
					
					String orgName = StringUtil.getString(sol.getLiteral("orgName"));
					String otherOrgName = StringUtil.getString(sol.getLiteral("otherOrgName"));
					
					if (orgName == null || otherOrgName == null)
						continue;
					
					int coopCount = sol.getLiteral("coopCount").getInt();
					
					newAllOrganizationCooperations.put(new StringCouple(orgName, otherOrgName), coopCount);
				}
				
				allOrganizationCooperation.putAll(newAllOrganizationCooperations);
			}
		}

		return allOrganizationCooperation;
	}
	
	private void loadConferenceOrganizationCooperationData(String conferenceAcronym) {
		if (organizationCooperationForConferenceAcronyms.containsKey(conferenceAcronym))
			return;
		
		Map<StringCouple, Integer> newConferenceData = new HashMap<>();
		ResultSet rs = RDFModel.getAllOrganisationPairsThatWroteAPaperTogetherFromGivenConference(conferenceAcronym);

		QuerySolution sol;
		while (rs.hasNext()) {
			sol = rs.next();
			String orgName = StringUtil.getString(sol.getLiteral("orgName"));
			String otherOrgName = StringUtil.getString(sol.getLiteral("otherOrgName"));
			
			if (orgName == null || otherOrgName == null)
				continue;
			
			int coopCount = sol.getLiteral("coopCount").getInt();
			
			newConferenceData.put(new StringCouple(orgName, otherOrgName), coopCount);
		}
		
		organizationCooperationForConferenceAcronyms.put(conferenceAcronym, newConferenceData);
	}
}
