package minerva.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import minerva.access.DirAccess;
import minerva.book.Book;
import minerva.seite.Breadcrumb;
import minerva.seite.Seite;
import minerva.seite.tag.TagNList;

public class BookSO {
	private final WorkspaceSO workspace;
	private final Book book;
	/** Seiten auf oberster Ebene */
	private SeitenSO seiten;
	
	public BookSO(WorkspaceSO workspace, Book book) {
		this.workspace = workspace;
		this.book = book;
		
		// Alle Seiten eines Buchs laden
		Map<String, String> files = workspace.dao().loadAllFiles(workspace.getFolder() + "/" + book.getFolder());
		List<Seite> alleSeiten = files.entrySet().stream()
				.filter(e -> e.getKey().endsWith(SeiteSO.META_SUFFIX))
				.map(e -> new Gson().fromJson(e.getValue(), Seite.class))
				.collect(Collectors.toList());

		seiten = SeitenSO.findeUnterseiten(getSeitensortierung(), alleSeiten, this);
	}
	
	// public for migration
	public Seitensortierung getSeitensortierung() {
		return new Seitensortierung() {
			@Override
			public String getId() {
				return SeiteSO.ROOT_ID;
			}

			@Override
			public boolean isSorted() {
				return book.isSorted();
			}

			@Override
			public String getUserLanguage() {
				return workspace.getUser().getLanguage();
			}
		};
	}

	// Seitensortierung und HasSeiten evtl. zusammenfassen?
	public HasSeiten getHasSeiten() {
		return new HasSeiten() {
			
			@Override
			public String getId() {
				return SeiteSO.ROOT_ID;
			}
			
			@Override
			public String getTitle() {
				return book.getTitle().getString(getUser().getLanguage());
			}
			
			@Override
			public SeitenSO getSeiten() {
				return seiten;
			}
		};
	}

	public SeitenSO getSeiten() {
		return seiten;
	}

	public Book getBook() {
		return book;
	}
	
	public String getFolder() {
		return workspace.getFolder() + "/" + book.getFolder();
	}
	
	public DirAccess dao() {
		return workspace.dao();
	}

	public WorkspaceSO getWorkspace() {
		return workspace;
	}

	public UserSO getUser() {
		return workspace.getUser();
	}
	
	public String createTopLevelSeite() {
		return seiten.createSeite(getSeitensortierung(), this);
	}
	
	public String getTitle() {
		return book.getTitle().getString(getUser().getLanguage());
	}

	public void activateSorted() {
		book.setSorted(true);
		saveBook();
	}
	
	private void saveBook() {
		BooksSO books = workspace.getBooks();
		books.incVersion();
		books.save("alphabetical sorting enabled for: " + getTitle());
		workspace.pull(); // ja, ist etwas brutal...
	}

	// similar method in SeiteSO
	public void savePagesAfterReordering(SeitenSO reorderdSeiten) {
		this.seiten = reorderdSeiten;
		Map<String, String> files = new HashMap<>();
		if (book.isSorted()) {
			book.setSorted(false);
			workspace.getBooks().saveTo(files);
		}
		reorderdSeiten.setPositionsAndSaveTo(files);
		dao().saveFiles(files, "reordering subpages of: " + getTitle(), workspace);
	}

	public List<SeiteSO> findTag(String tag) {
		List<SeiteSO> ret = new ArrayList<>();
		for (SeiteSO seite : seiten) {
			ret.addAll(seite.findTag(tag));
		}
		return ret;
	}

	public void addAllTags(TagNList tags) {
		seiten.forEach(seite -> seite.addAllTags(tags));
	}

	public List<Breadcrumb> getBreadcrumbs(String seiteId) {
		List<Breadcrumb> breadcrumbs = new ArrayList<>();
		if (seiten.breadcrumbs(seiteId, breadcrumbs)) {
			Breadcrumb b = new Breadcrumb();
			b.setTitle(book.getTitle());
			b.setLink("/b/" + workspace.getBranch() + "/" + book.getFolder());
			breadcrumbs.add(b);
		}
		return breadcrumbs;
	}
}
