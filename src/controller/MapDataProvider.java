package controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import log.LogManager;
import log.Logger;
import rdf.RDFModel;
import util.StringCouple;
import util.StringUtil;
import util.location.CountryLocationCache;
import util.location.OrganizationCountryMap;
import util.location.OrganizationLocationCache;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.fhpotsdam.unfolding.geo.Location;

public class MapDataProvider extends DataProvider implements ui.map.DataProvider {
	
	private static final Logger logger = LogManager.getLogger(MapDataProvider.class);

	private final CountryLocationCache countryLocationCache;
	private final OrganizationLocationCache organizationLocationCache;
	private final OrganizationCountryMap organizationCountryMap;

	private final SortedSet<String> allOrganizations;
	private final Map<StringCouple, Integer> allOrganizationCooperation;
	private final Map<String, Map<StringCouple, Integer>> organizationCooperationForConferenceAcronyms;
	private final Map<StringCouple, Set<StringCouple>> organizationCooperationForCountries;
	private final Map<String, SortedSet<String>> countryOrganizations;

	public MapDataProvider(String organizationLocationFile, String countryLocationFile, String organizationCountryFile)
			throws IOException {
		countryLocationCache = new CountryLocationCache(countryLocationFile);
		organizationLocationCache = new OrganizationLocationCache(organizationLocationFile);
		organizationCountryMap = new OrganizationCountryMap(organizationCountryFile);
		
		allOrganizations = new TreeSet<>();
		allOrganizationCooperation = new HashMap<>();
		organizationCooperationForConferenceAcronyms = new HashMap<>();
		organizationCooperationForCountries = new HashMap<>();
		countryOrganizations = new HashMap<>();
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

	@Override
	public SortedSet<String> getOrganizations(String country) {
		if (countryOrganizations.isEmpty()) {
			synchronized (countryOrganizations) {
				if (!countryOrganizations.isEmpty()) {
					return countryOrganizations.get(country.trim());
				}
				
				Set<String> allOrganizations = getAllOrganizations();
				Map<String, SortedSet<String>> newCountryOrganizations = new HashMap<>();
				
				logger.debug("Storing %d organizations in the country -> organizations map", allOrganizations.size());
				for (String organization : allOrganizations) {
					String orgCountry = getCountry(organization);
					
					if (orgCountry == null) {
						logger.error("Unable to find country for organization %s", organization);
						continue;
					}
					
					if (!newCountryOrganizations.containsKey(orgCountry)) {
						logger.trace("Adding country %s", orgCountry);
						newCountryOrganizations.put(orgCountry, new TreeSet<String>());
					}
					newCountryOrganizations.get(orgCountry).add(organization);
				}
				
				logger.debug("That is in %d countries", newCountryOrganizations.size());
				countryOrganizations.putAll(newCountryOrganizations);
			}
		}
		
		return countryOrganizations.get(country.trim());
	}

	@Override
	public Set<StringCouple> getOrganizationCooperationForCountries(StringCouple countries) {
		if (organizationCooperationForCountries.isEmpty()) {
			synchronized(organizationCooperationForCountries) {
				if (!organizationCooperationForCountries.isEmpty()) {
					return organizationCooperationForCountries.get(countries);
				}
				
				// don't load the data directly, but from the organizationCooperationData
				Set<StringCouple> allOrganizationCooperations = getAllOrganizationCooperationData().keySet();
				
				Map<StringCouple, Set<StringCouple>> newOrganizatinCooperationForCountries = new HashMap<>();
				
				for (StringCouple organizationCooperation : allOrganizationCooperations) {
					String country1 = getCountry(organizationCooperation.getString1());
					String country2 = getCountry(organizationCooperation.getString2());
					
					if (country1 == null) {
						logger.error("Cannot find country for organization %s", organizationCooperation.getString1());
						continue;
					}
					
					if (country2 == null) {
						logger.error("Cannot find country for organization %s", organizationCooperation.getString2());
						continue;
					}
					
					StringCouple countryCooperation = new StringCouple(country1, country2);
					
					if (!newOrganizatinCooperationForCountries.containsKey(countryCooperation)) {
						newOrganizatinCooperationForCountries.put(countryCooperation, new HashSet<StringCouple>());
					}
					newOrganizatinCooperationForCountries.get(countryCooperation).add(organizationCooperation);
				}
				
				organizationCooperationForCountries.putAll(newOrganizatinCooperationForCountries);
			}
		}
		return organizationCooperationForCountries.get(countries);
	}
}
