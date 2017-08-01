package com.kevin.filemanager;

import android.app.*;
import android.os.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;
import android.widget.*;
import android.widget.AdapterView.*;
import android.util.Log;
import android.content.*;
import android.view.*;
import android.graphics.drawable.Drawable;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.net.Uri;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import android.graphics.Bitmap;


public class MainActivity extends Activity 
{
	public static Context mcontext;
	final File sdcard = new File("/sdcard");
	public static List<File> allFiles;
	private File temp = null;
	private ListView filelistview;
	public File currentfile;
	private TextView currentinfo;
	private RelativeLayout toolbar;
	private ImageButton copybtn, cutbtn, cancelbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mcontext = this;
		filelistview = (ListView)findViewById(R.id.filelistview);
		currentinfo = (TextView) findViewById(R.id.currentinfo);
		toolbar = (RelativeLayout) findViewById(R.id.toolbar);
		copybtn = (ImageButton) findViewById(R.id.copybtn);
		cutbtn = (ImageButton) findViewById(R.id.cutbtn);
		cancelbtn = (ImageButton) findViewById(R.id.cancelbtn);
		currentfile = sdcard;
		filelistview.setOnItemClickListener(filelistclick);
		filelistview.setOnItemLongClickListener(filelistlongclick);
		copybtn.setOnClickListener(toolclick);
		cutbtn.setOnClickListener(toolclick);
		cancelbtn.setOnClickListener(toolclick);
		getfile(sdcard);


    }

	OnItemClickListener filelistclick = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> view , View v, int position, long id)
		{
			getfile(allFiles.get(position));

		}
	};

	OnClickListener toolclick = new OnClickListener(){

		@Override
		public void onClick(View p1)
		{
			if (p1.getId() == R.id.copybtn)
			{
				final ProgressDialog p = new ProgressDialog(MainActivity.this);
				p.setTitle("加载中");
				p.setMessage("请稍后...");
				p.show();
				new Thread(new Runnable(){
						@Override
						public void run()
						{
							try
							{
								copyfile(temp, new File(currentfile, temp.getName()));
								runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											Toast.makeText(getApplicationContext(), "复制成功", Toast.LENGTH_LONG).show();
											getfile(currentfile);

										}
									});
							}
							catch (Exception e)
							{
								e.printStackTrace();
								runOnUiThread(new Runnable(){
										@Override
										public void run()
										{
											Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
										}
									});
							}
							finally
							{
								runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											p.dismiss();
											temp = null;
											toolbar.setVisibility(View.GONE);
										}
									});
							}
						}
					}).start();

			}
			else if (p1.getId() == R.id.cutbtn)
			{
				new Thread(new Runnable(){
						@Override
						public void run()
						{
							try
							{
								if (currentfile.getPath().equals(temp.getPath()))
								{
									runOnUiThread(new Runnable(){
										@Override
										public void run()
										{
											Toast.makeText(getApplicationContext(), "无法将此文件夹剪切到它的子文件夹里", Toast.LENGTH_SHORT).show();
										}
									});									
									return;
								}
								temp.renameTo(new File(currentfile.getPath() + "/" + temp.getName()));
								runOnUiThread(new Runnable(){
										@Override
										public void run()
										{
											Toast.makeText(getApplicationContext(), "剪切成功", Toast.LENGTH_SHORT).show();
											getfile(currentfile);
										}
									});
							}
							catch (Exception e)
							{
								e.printStackTrace();
								runOnUiThread(new Runnable(){
										@Override
										public void run()
										{
											Toast.makeText(getApplicationContext(), "剪切失败", Toast.LENGTH_SHORT).show();
										}
									});

							}
							finally
							{
								runOnUiThread(new Runnable(){
										@Override
										public void run()
										{
											temp = null;
											toolbar.setVisibility(View.GONE);
										}
									});
							}
						}
					}).start();

			}
			else if (p1.getId() == R.id.cancelbtn)
			{
				toolbar.setVisibility(View.GONE);
				temp = null;
			}
		}

	};

	OnItemLongClickListener filelistlongclick = new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			final File t = allFiles.get(p3);
			AlertDialog.Builder longdig = new AlertDialog.Builder(MainActivity.this);
			String[] choices = {"多选", "删除", "重命名", "复制", "剪切", "属性"};
			longdig.setItems(choices, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int position)
					{
						switch (position)
						{
							case 0:
								//filelistview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
								break;
							case 1:
								final ProgressDialog p = new ProgressDialog(MainActivity.this);
								p.setTitle("删除中");
								p.setMessage("请稍后...");
								p.show();
								new Thread(new Runnable(){
										@Override
										public void run()
										{
											try
											{
												delete(t);
											}
											catch (Exception e)
											{
												e.printStackTrace();
												runOnUiThread(new Runnable(){
														@Override
														public void run()
														{
															p.dismiss();
															Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_LONG).show();
														}
													});

											}
											runOnUiThread(new Runnable(){
													@Override
													public void run()
													{
														p.dismiss();
														getfile(currentfile);
														Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_LONG).show();
													}
												});
										}
									}).start();
								break;
							case 2:
								final EditText newname = new EditText(MainActivity.this);
								newname.setText(t.getName());
								AlertDialog.Builder renamedig = new AlertDialog.Builder(MainActivity.this);
								renamedig.setTitle("新名称");
								renamedig.setView(newname);
								renamedig.setPositiveButton("确定", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface d, int i)
										{
											try
											{
												t.renameTo(new File(currentfile.getPath() + "/" + newname.getText()));
												getfile(currentfile);
											}
											catch (Exception e)
											{
												e.printStackTrace();
											}
										}
									});

								renamedig.setNegativeButton("取消", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface d, int i)
										{
											d.dismiss();
										}
									});
								renamedig.show();
								break;
							case 3:
								toolbar.setVisibility(View.VISIBLE);
								copybtn.setVisibility(View.VISIBLE);
								cutbtn.setVisibility(View.GONE);
								temp = t;
								break;
							case 4:
								toolbar.setVisibility(View.VISIBLE);
								copybtn.setVisibility(View.GONE);
								cutbtn.setVisibility(View.VISIBLE);
								temp = t;
								break;
							case 5:
								AlertDialog.Builder infodig = new AlertDialog.Builder(MainActivity.this);
								infodig.setTitle("详细信息");
								infodig.setMessage("名称：" + t.getName() + "\n\n" + "路径：" + t.getPath() + "\n\n" + "大小：" + arrayadapter.getsize((float)t.length()));
								infodig.show();
								break;
						}
					}
				});
			longdig.show();
			return true;
		}

	};

	private void getfile(File f)
	{
		if (f.isDirectory())
		{
			currentfile = f;
			currentinfo.setText(" 当前路径：" + f.getPath());
			allFiles = new ArrayList<File>();
			File[] files = f.listFiles();
			Arrays.sort(files);
			if (files != null)
			{
				for (File t : files)
					if (t.isDirectory())
						allFiles.add(t);
				for (File t : files)
					if (t.isFile())
						allFiles.add(t);
			}

			filelistview.setAdapter(new arrayadapter(MainActivity.this, allFiles));
		}
		else
		{
			openfile(f);
		}
	}
	@Override
	public void onBackPressed()
	{
		//super.onBackPressed();
		if (!currentfile.getPath().equals("/sdcard"))
		{
			getfile(currentfile.getParentFile());
			filelistview.setAdapter(new arrayadapter(MainActivity.this, allFiles));
		}
		else
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.exit)
		{
			System.exit(0);
		}
		else
		{
			final EditText filename = new EditText(MainActivity.this);
			AlertDialog.Builder newf = new AlertDialog.Builder(MainActivity.this);
			newf.setTitle("名称");
			newf.setView(filename);
			newf.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface d, int i)
					{
						File n = new File(currentfile.getPath() + "/" + filename.getText());
						switch (item.getItemId())
						{
							case R.id.createfile:
								try
								{
									if (!n.exists())
										n.createNewFile();
									else
									{
										Toast.makeText(getApplicationContext(), "文件已存在", Toast.LENGTH_SHORT).show();
										return;
									}
									getfile(currentfile);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								break;
							case R.id.createfolder:
								try
								{
									if (!n.exists())
										n.mkdir();
									else
									{
										Toast.makeText(getApplicationContext(), "文件夹已存在", Toast.LENGTH_SHORT).show();
										return;
									}
									getfile(currentfile);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								break;
						}
					}
				});

			newf.setNegativeButton("取消", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface d, int i)
					{
						d.dismiss();
					}
				});
			newf.show();

			return true;
		}
		return true;
	}

	private void copyfile(File source, File target)
	throws Exception
	{
		if (source.isDirectory())
		{
			if (!target.getPath().contains(source.getPath()))
			{
				if (!target.exists())
					target.mkdir();
				String[] child = source.list();
				for (int i = 0; i < source.listFiles().length; i++)
				{
					copyfile(new File(source, child[i]), new File(target, child[i]));
				}
			}
			else
				runOnUiThread(new Runnable(){
						@Override
						public void run()
						{
							Toast.makeText(getApplicationContext(), "无法复制到它的子文件夹里", Toast.LENGTH_LONG).show();
						}
					});
		}
		else
		{
			FileInputStream in;
			FileOutputStream out;
			if (!source.getPath().equals(target.getPath()))
			{
				in = new FileInputStream(source);
				out = new FileOutputStream(target);
			}
			else
			{
				in = new FileInputStream(source);
				out = new FileOutputStream(new File(target.getPath() + "副本"));
			}
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
				out.write(buffer, 0, read);

			in.close();
			out.close();
		}
	}

	private void delete(File f)
	{
		if (f.isDirectory())
		{
			for (File child : f.listFiles())
			{
				delete(child);
			}
		}
		f.delete();
	}

	private void openfile(File url)
	{
		Uri uri = Uri.fromFile(url);
		Intent intent = new Intent(Intent.ACTION_VIEW);
        if (url.toString().contains(".doc") || url.toString().contains(".docx"))
		{
            // Word document
            intent.setDataAndType(uri, "application/msword");
        }
		else if (url.toString().contains(".pdf"))
		{
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        }
		else if (url.toString().contains(".ppt") || url.toString().contains(".pptx"))
		{
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        }
		else if (url.toString().contains(".xls") || url.toString().contains(".xlsx"))
		{
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        }
		else if (url.toString().contains(".zip") || url.toString().contains(".rar"))
		{
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        }
		else if (url.toString().contains(".apk"))
		{
			intent.setDataAndType(uri, "application/vnd.android.package-archive");
		}
		else if (url.toString().contains(".rtf"))
		{
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        }
		else if (url.toString().contains(".wav") || url.toString().contains(".mp3"))
		{
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        }
		else if (url.toString().contains(".gif"))
		{
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        }
		else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png"))
		{
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        }
		else if (url.toString().contains(".txt"))
		{
            // Text file
            intent.setDataAndType(uri, "text/plain");
        }
		else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi"))
		{
            // Video files
            intent.setDataAndType(uri, "video/*");
        }
		else
		{
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        MainActivity.this.startActivity(intent);
	}

}
class arrayadapter extends ArrayAdapter
{
	ImageLoader imageLoader;
	DisplayImageOptions options;
	private MainActivity context;
	private List<File> files;
	private Drawable iconfolder, iconuknownfile, iconfile, playable;
	arrayadapter(MainActivity context, List<File> files)
	{
		super(context, 0, files);
		this.context = context;
		this.files = files;
		iconfolder = context.getResources().getDrawable(R.drawable.folder);
		iconuknownfile = context.getResources().getDrawable(R.drawable.uknownfile);
		iconfile = context.getResources().getDrawable(R.drawable.file);
		playable = context.getResources().getDrawable(R.drawable.video);
		options = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .cacheInMemory(true)
			.showImageOnLoading(R.drawable.pic)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY)
            .resetViewBeforeLoading(true)
            .build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
            .defaultDisplayImageOptions(options)
            .threadPriority(Thread.MAX_PRIORITY)
            .threadPoolSize(5)
            .diskCacheSize(100 * 1024 * 1024)
            .memoryCache(new WeakMemoryCache())
            .denyCacheImageMultipleSizesInMemory()
            .build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO: Implement this method
		ViewHolder view;
		if (convertView == null)
		{
			view = new ViewHolder();
			convertView = context.getLayoutInflater().inflate(R.layout.file, null);
			view.fileimage = (ImageView) convertView.findViewById(R.id.fileimage);
			view.filetext = (TextView) convertView.findViewById(R.id.filename);
			view.fileinfo = (TextView) convertView.findViewById(R.id.fileinfo);
			convertView.setTag(view);
		}
		else
			view = (ViewHolder)convertView.getTag();
		File t = files.get(position);
		if (t.isDirectory())
			view.fileimage.setImageDrawable(iconfolder);
		else
		{
			if (t.toString().contains(".jpg") || t.toString().contains(".png"))
				imageLoader.displayImage("file://" + t.getPath(), view.fileimage);
			else if (t.toString().contains(".txt") || t.toString().contains(".json") || t.toString().contains(".xml") || t.toString().contains(".log") || t.toString().contains(".java") || t.toString().contains(".c") || t.toString().contains(".cpp") || t.toString().contains(".cs"))
				view.fileimage.setImageDrawable(iconfile);
			else if (t.toString().contains(".mp3")  || t.toString().contains(".mp4") || t.toString().contains(".3gp") || t.toString().contains(".avi") || t.toString().contains(".wav") || t.toString().contains(".amr") || t.toString().contains(".ape") || t.toString().contains(".flac") || t.toString().contains(".rmvb"))
			{
				view.fileimage.setImageDrawable(playable);
			}
			else
				view.fileimage.setImageDrawable(iconuknownfile);
		}
		view.filetext.setText(files.get(position).getName());
		view.fileinfo.setText(getsize((float)t.length()));
		return convertView;
	}

	public static String getsize(float size)
	{
		if (size / 1073741824 >= 1)
			return String.format("%.2f", size / 1073741824) + "GB";
		else if (size / 1048576 >= 1)
			return String.format("%.2f", size / 1048576) + "MB";
		else if (size / 1024 >= 1)
			return String.format("%.2f", size / 1024) + "KB";
		else
			return size + "B";
	}

}
class ViewHolder
{
	ImageView fileimage;
	TextView filetext;
	TextView fileinfo;
}

