/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileServices;

import java.io.File;
import java.io.FilenameFilter;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author haris
 */
public class Share
{
	/*Make the files under the directory available for sharing*/

	protected TreeMap<String, String> filenames;
	private int numberOfFiles;

	public Share(String dir)
	{
		filenames = new TreeMap<String, String>();
		visitAllFiles(new File(dir));
		numberOfFiles = filenames.size();
	}

	/*To be removed. Testing purposes*/
	public void print()
	{
		Iterator it = filenames.entrySet().iterator();
		while (it.hasNext())
		{
			Entry relation = (Entry) it.next();

			System.out.println(relation.getKey() + " | " + relation.getValue());
		}


	}

	public String[] getFilenames()
	{
		Iterator it = filenames.entrySet().iterator();
		String fileArray[] = new String[filenames.size()];

		int i = 0;

		while (it.hasNext())
		{
			Entry relation = (Entry) it.next();

			fileArray[i] = relation.getKey().toString();
			i++;
		}

		return fileArray;

	}

	private void visitAllFiles(File dir)
	{

		/*Filter all filenames, blocking hidden files*/
		FilenameFilter filter = new FilenameFilter()
		{

			public boolean accept(File dir, String name)
			{
				return !name.startsWith(".");
			}
		};

		File[] files = dir.listFiles(filter);

		if (files == null)
		{
			return;
		}

		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isDirectory())
			{
				visitAllFiles(new File(files[i].getAbsoluteFile().toString()));
			}
			else
			{
				addFile(files[i].getName(), files[i].getAbsolutePath().toString());
			}
		}
	}

	/**
	 * Add the specified file to the share collection
	 * @param Filename
	 * @return false if file already exists.
	 */
	public void addFile(String filename, String path)
	{
		filenames.put(filename, path);
		this.numberOfFiles++;
	}

	/**
	 * Removes the specified file to the share collection
	 * @param Filename
	 * @return false if file does not exist.
	 */
	public void removeFile(String filename)
	{
		filenames.remove(filename);
		this.numberOfFiles--;
	}

	public int getNumberOfFiles()
	{
		return numberOfFiles;
	}

	public void setNumberOfFiles(int numberOfFiles)
	{
		this.numberOfFiles = numberOfFiles;
	}
}
