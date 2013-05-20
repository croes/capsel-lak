package controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import log.LogManager;
import log.Logger;
import rdf.RDFModel;
import ui.ChartSelectionPanel;
import util.StringUtil;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

/**
 * FIXME K.U.Leuven and KULeuven are both changed to KU Leuven by StringUtil,
 * resulting in wrong results !!!
 */
public class ChartDataProvider implements ChartSelectionPanel.DataProvider, MapController.DataProvider {

	private static final Logger logger = LogManager.getLogger(ChartDataProvider.class);

	protected static class ConfYearCouple {
		protected final String conference;
		protected final int year;

		public ConfYearCouple(String conference, int year) {
			this.conference = conference;
			this.year = year;
		}
		
		public ConfYearCouple(String conferenceAcronym) {
			this.conference = conferenceAcronym.replaceAll("[0-9]{4}$", "");
			this.year = Integer.valueOf(conferenceAcronym.substring(conference.length()), 10);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o == this)
				return true;
			if (!(o instanceof ConfYearCouple))
				return false;

			ConfYearCouple other = (ConfYearCouple) o;

			return conference.equals(other.conference) && (year == other.year);
		}

		@Override
		public int hashCode() {
			return conference.hashCode() * 31 ^ year;
		}
	}

	protected static class ConfYearOrgTriple extends ConfYearCouple {

		protected final String organization;

		public ConfYearOrgTriple(String organization, String conference, int year) {
			super(conference, year);
			this.organization = organization;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o == this)
				return true;
			if (!(o instanceof ConfYearOrgTriple))
				return false;

			if (!super.equals(o))
				return false;

			ConfYearOrgTriple other = (ConfYearOrgTriple) o;
			return organization.equals(other.organization);
		}

		@Override
		public int hashCode() {
			return super.hashCode() * 31 ^ organization.hashCode();
		}

	}

	protected final SortedSet<String> conferences;

	protected final Map<ConfYearCouple, Double> conferenceData;
	protected final Map<ConfYearCouple, SortedSet<String>> conferenceOrganizationMap;
	protected final Map<ConfYearOrgTriple, Double> organizationData;

	public ChartDataProvider() {
		conferences = new TreeSet<>();
		conferenceData = new HashMap<>();
		organizationData = new HashMap<>();
		conferenceOrganizationMap = new HashMap<>();
	}

	@Override
	public double getConferenceData(String conference, int year) {
		ConfYearCouple key = new ConfYearCouple(conference, year);

		if (!conferenceData.containsKey(key)) {
			synchronized (conferenceData) {
				if (conferenceData.containsKey(key))
					return conferenceData.get(key);

				logger.debug("Getting conference data for conference %s, year %d", conference, year);

				double data = 0;
				for (String organization : getOrganizationsForConference(conference, year)) {
					data += getOrganizationData(conference, organization, year);
				}

				conferenceData.put(key, data);
				return data;
			}
		}

		return conferenceData.get(key);
	}

	@Override
	public double getOrganizationData(String conference, String organization, int year) {
		return Math.max(0, getOrganizationData2(conference, organization, year));
	}

	private double getOrganizationData2(String conference, String organization, int year) {
		ConfYearOrgTriple key = new ConfYearOrgTriple(organization, conference, year);
		if (!organizationData.containsKey(key)) {
			synchronized (organizationData) {
				if (organizationData.containsKey(key))
					return organizationData.get(key);

				logger.debug("Getting organization data for organization %s, conference %s, year %d", organization,
						conference, year);

				String confAcronym = conference + Integer.toString(year, 10);
				int data = RDFModel.getPaperCount(confAcronym, organization);
				
				for (String misspelledOrganization : StringUtil.getWrongStrings(organization))
					data += RDFModel.getPaperCount(confAcronym, misspelledOrganization);
				
				organizationData.put(key, (double) data);
				return data;
			}
		}

		return organizationData.get(key);
	}

	@Override
	public int getMinYear() {
		// TODO
		return 2008;
	}

	@Override
	public int getMaxYear() {
		// TODO
		return 2012;
	}

	@Override
	public SortedSet<String> getConferences() {
		if (conferences.isEmpty()) {
			synchronized (conferences) {
				if (!conferences.isEmpty())
					return conferences;

				logger.debug("Getting all conferences");

				List<RDFNode> confData = RDFModel.getConferences();
				for (RDFNode conf : confData) {
					String confAcronym = StringUtil.getString(conf.asResource().getProperty(
							RDFModel.getModel().getProperty("http://data.semanticweb.org/ns/swc/ontology#hasAcronym")));

					conferences.add(confAcronym.replaceAll("[0-9]", ""));
				}
			}
		}

		return conferences;
	}

	@Override
	public boolean hasConferenceTakenPlace(String conference, int year) {
		return getConferenceData(conference, year) > 0;
	}

	@Override
	public SortedSet<String> getOrganizationsForConference(String conference, int year) {
		ConfYearCouple key = new ConfYearCouple(conference, year);
		if (!conferenceOrganizationMap.containsKey(key)) {
			synchronized (conferenceOrganizationMap) {
				if (conferenceOrganizationMap.containsKey(key))
					conferenceOrganizationMap.get(key);

				logger.debug("Getting organizations for conference %s, year %d", conference, year);

				SortedSet<String> organizations = new TreeSet<>();
				String confAcronym = conference + Integer.toString(year, 10);

				for (RDFNode node : RDFModel.getOrganisationsOfConference(confAcronym)) {
					organizations.add(StringUtil.getString(node.asResource().getProperty(FOAF.name)));
				}

				conferenceOrganizationMap.put(key, organizations);
				return organizations;
			}
		}

		return conferenceOrganizationMap.get(key);
	}

	@Override
	public SortedSet<String> getOrganizationsForConferences(Collection<String> conferences, Collection<Integer> years) {
		SortedSet<String> result = new TreeSet<>();

		for (String conference : conferences) {
			for (int year : years) {
				if (hasConferenceTakenPlace(conference, year)) {
					result.addAll(getOrganizationsForConference(conference, year));
				}
			}
		}

		return result;
	}

	@Override
	public SortedSet<String> getOrganizationsForConferences(Collection<String> conferences, int year) {
		SortedSet<String> result = new TreeSet<>();

		for (String conference : conferences) {
			if (hasConferenceTakenPlace(conference, year)) {
				result.addAll(getOrganizationsForConference(conference, year));
			}
		}

		return result;
	}

	@Override
	public SortedSet<String> getOrganizationsForConferences(String conference, Collection<Integer> years) {
		SortedSet<String> result = new TreeSet<>();

		for (int year : years) {
			if (hasConferenceTakenPlace(conference, year)) {
				result.addAll(getOrganizationsForConference(conference, year));
			}
		}

		return result;
	}

}
