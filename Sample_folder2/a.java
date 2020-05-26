import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;



public class DiffFilesInCommit {

    public static void main(String[] args) throws Exception {
    	String REMOTE_URL = "https://github.com/kRhythm/SampleCode.git";
    	Git result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setDirectory(new File("C:\\Users\\nvuggam\\Desktop\\DCA"))
                .call();

    	try (Repository repository = result.getRepository()) {
            
            Collection<Ref> allRefs = repository.getAllRefs().values();

            try (RevWalk revWalk = new RevWalk( repository )) {
                for( Ref ref : allRefs ) {
                    revWalk.markStart( revWalk.parseCommit( ref.getObjectId() ));
                }
                System.out.println("Walking all commits starting with " + allRefs.size() + " refs: " + allRefs);
                int count = 0;
                RevCommit previouscommit = null;
                RevCommit presentcommit = null;
                for( RevCommit commit : revWalk ) 
                {
                	if(count==0) {
                		previouscommit = commit;
                	}
                	else
                	{
                		presentcommit = commit;
                		try (Git git = new Git(repository)) 
                		{
                			RevTree previoustree = previouscommit.getTree();
                			RevTree presenttree = presentcommit.getTree();
                			File FileInfo= new File("C:\\Users\\nvuggam\\Desktop\\FileInfo.txt");
                			PrintStream consolee = System.out; 
                	    	PrintStream hi = new PrintStream(FileInfo);
                	    	System.setOut(hi);
                			listDiff(repository, git,presentcommit,previouscommit);                			
                		}
                        previouscommit = presentcommit;
                	}
                    count++;
                }
            }
        }
    }
    private class void sampleClass()
    {
        int h = 0;
        int j = 1;
        //sample_me = sample_you ;
    }
    
    private static void listDiff(Repository repository, Git git, ObjectId oldCommit, ObjectId newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        //System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println(diff.getChangeType() + " " + diff.getOldPath() + " " + diff.getNewPath() );
        }
    }
    
    private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(objectId);
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
    
    
}    
    
        
  
