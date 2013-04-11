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

import com.hp.hpl.jena.query.ResultSet;

import de.fhpotsdam.unfolding.geo.Location;

public class MapDataProvider extends DataProvider implements AbstractLAKMap.DataProvider {

	private final CountryLocationCache countryLocationCache;
	private final OrganizationLocationCache organizationLocationCache;
	private final OrganizationCountryMap organizationCountryMap;

	private final SortedSet<String> allOrganizations;

	public MapDataProvider(String organizationLocationFile, String countryLocationFile, String organizationCountryFile)
			throws IOException {
		countryLocationCache = new CountryLocationCache(countryLocationFile);
		organizationLocationCache = new OrganizationLocationCache(organizationLocationFile);
		organizationCountryMap = new OrganizationCountryMap(organizationCountryFile);
		
		allOrganizations = new TreeSet<>();
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
		
		// TODO
		
		return organizationCooperation;
	}

	@Override
	public Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym) {
		return getOrganizationCooperationDataForConference(conferenceAcronym, null);
	}

	@Override
	public Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym,
			Map<StringCouple, Integer> data) {
		if (data == null) {
			data = new HashMap<>();
		}
		
		// TODO 
		
		return data;
	}

	@Override
	public Map<StringCouple, Integer> getAllOrganizationCooperationData() {
		// TODO Auto-generated method stub
		return null;
	}
}
