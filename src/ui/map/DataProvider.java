package ui.map;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import util.StringCouple;
import de.fhpotsdam.unfolding.geo.Location;

public interface DataProvider {

	Location getCountryLocation(String country);

	Location getOrganizationLocation(String organization);

	String getCountry(String organization);

	SortedSet<String> getOrganizations(String country);

	SortedSet<String> getAllOrganizations();

	/**
	 * Just a call to
	 * <code>getOrganizationsForConference(conferenceAcronym, null);</code>
	 * 
	 * @see #getOrganizationsForConference(String, Set)
	 */
	Set<String> getOrganizationsForConference(String conferenceAcronym);

	/**
	 * Returns a set containing the organizations for the given conference.
	 * If the set given is not <code>null</code>, the data is added to that
	 * set and the set is returned. Otherwise, a new set object is returned.
	 */
	Set<String> getOrganizationsForConference(String conferenceAcronym, Set<String> organizations);

	Set<StringCouple> getOrganizationCooperationsForConference(String conferenceAcronym);

	Set<StringCouple> getOrganizationCooperationsForConference(String conferenceAcronym,
			Set<StringCouple> organizationCooperation);
	
	Set<StringCouple> getOrganizationCooperationForCountries(StringCouple countries);

	Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym);

	Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym,
			Map<StringCouple, Integer> data);

	Map<StringCouple, Integer> getAllOrganizationCooperationData();
}